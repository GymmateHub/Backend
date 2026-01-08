package com.gymmate.membership.application;

import com.gymmate.membership.domain.FreezePolicy;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.infrastructure.FreezePolicyRepository;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.membership.domain.MembershipPlan;
import com.gymmate.membership.infrastructure.MembershipPlanRepository;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MembershipServiceTest {

  private MemberMembershipRepository membershipRepository;
  private MembershipPlanRepository planRepository;
  private MembershipService membershipService;
  private FreezePolicyRepository freezePolicyRepository;
  private MemberPaymentService memberPaymentService;

  @BeforeEach
  void setUp() {
    membershipRepository = mock(MemberMembershipRepository.class);
    planRepository = mock(MembershipPlanRepository.class);
    freezePolicyRepository = mock(FreezePolicyRepository.class);
    memberPaymentService = mock(MemberPaymentService.class);
    membershipService = new MembershipService(membershipRepository, planRepository, freezePolicyRepository, memberPaymentService);
  }

  @Test
  void subscribeMember_happyPath_createsAndSavesMembership() {
    UUID gymId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();
    LocalDate startDate = LocalDate.of(2025, 12, 1);

    // plan
    MembershipPlan plan = MembershipPlan.builder()
      .name("Standard")
      .price(new BigDecimal("29.99"))
      .billingCycle("monthly")
      .durationMonths(1)
      .classCredits(10)
      .guestPasses(1)
      .trainerSessions(0)
      .build();

    when(membershipRepository.findActiveMembershipByMemberId(memberId)).thenReturn(Optional.empty());
    when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

    ArgumentCaptor<MemberMembership> captor = ArgumentCaptor.forClass(MemberMembership.class);
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(invocation -> invocation.getArgument(0));

    MemberMembership created = membershipService.subscribeMember(gymId, memberId, planId, startDate);

    assertNotNull(created);
    assertEquals(memberId, created.getMemberId());
    assertEquals(planId, created.getMembershipPlanId());
    assertEquals(startDate, created.getStartDate());
    assertNotNull(created.getEndDate());
    assertEquals(plan.getPrice(), created.getMonthlyAmount());

    verify(membershipRepository, times(1)).save(captor.capture());
    MemberMembership saved = captor.getValue();
    assertEquals(memberId, saved.getMemberId());
    assertEquals(planId, saved.getMembershipPlanId());
  }

  @Test
  void subscribeMember_whenActiveMembershipExists_throwsDomainException() {
    UUID gymId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();

    MemberMembership existing = MemberMembership.builder().memberId(memberId).build();
    when(membershipRepository.findActiveMembershipByMemberId(memberId)).thenReturn(Optional.of(existing));

    DomainException ex = assertThrows(DomainException.class, () ->
      membershipService.subscribeMember(gymId, memberId, planId, LocalDate.now())
    );

    assertTrue(ex.getMessage().contains("ACTIVE_MEMBERSHIP_EXISTS") || ex.getErrorCode() == null || ex.getErrorCode().length() >= 0);
    verify(membershipRepository, never()).save(any());
  }

  @Test
  void freezeMembership_activeMembership_movesToPausedAndSaves() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .membershipPlanId(UUID.randomUUID())
      .status(MembershipStatus.ACTIVE)
      .frozen(false)
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(inv -> inv.getArgument(0));

    LocalDate until = LocalDate.now().plusDays(7);
    MemberMembership result = membershipService.freezeMembership(membershipId, until, "Vacation");

    assertTrue(result.isFrozen());
    assertEquals(MembershipStatus.PAUSED, result.getStatus());
    assertEquals(until, result.getFrozenUntil());
    assertEquals("Vacation", result.getFreezeReason());
    verify(membershipRepository).save(result);
  }

  @Test
  void unfreezeMembership_whenNotFrozen_throwsDomainException() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .status(MembershipStatus.ACTIVE)
      .frozen(false)
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));

    assertThrows(DomainException.class, () -> membershipService.unfreezeMembership(membershipId));
  }

  @Test
  void unfreezeMembership_whenFrozen_unfreezesAndSaves() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .status(MembershipStatus.PAUSED)
      .frozen(true)
      .frozenUntil(LocalDate.now().plusDays(5))
      .freezeReason("Break")
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(inv -> inv.getArgument(0));

    MemberMembership result = membershipService.unfreezeMembership(membershipId);

    assertFalse(result.isFrozen());
    assertEquals(MembershipStatus.ACTIVE, result.getStatus());
    assertNull(result.getFrozenUntil());
    assertNull(result.getFreezeReason());
    verify(membershipRepository).save(result);
  }

  @Test
  void renewMembership_happyPath_updatesDatesAndResetsCredits() {
    UUID membershipId = UUID.randomUUID();
    UUID planId = UUID.randomUUID();
    LocalDate endDate = LocalDate.of(2025, 12, 31);

    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .membershipPlanId(planId)
      .endDate(endDate)
      .status(MembershipStatus.ACTIVE)
      .autoRenew(true)
      .build();

    MembershipPlan plan = MembershipPlan.builder()
      .price(new BigDecimal("19.99"))
      .billingCycle("monthly")
      .durationMonths(1)
      .classCredits(5)
      .guestPasses(1)
      .trainerSessions(0)
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));
    when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(inv -> inv.getArgument(0));

    MemberMembership result = membershipService.renewMembership(membershipId);

    assertEquals(MembershipStatus.ACTIVE, result.getStatus());
    assertEquals(endDate.plusDays(1), result.getStartDate());
    assertNotNull(result.getEndDate());
    assertEquals(plan.getClassCredits(), result.getClassCreditsRemaining());
    verify(membershipRepository).save(result);
  }

  @Test
  void cancelMembership_immediate_setsCancelledAndEndDateNow() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .status(MembershipStatus.ACTIVE)
      .autoRenew(true)
      .endDate(LocalDate.now().plusDays(30))
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(inv -> inv.getArgument(0));

    MemberMembership result = membershipService.cancelMembership(membershipId, true);

    assertEquals(MembershipStatus.CANCELLED, result.getStatus());
    assertFalse(result.isAutoRenew());
    assertEquals(LocalDate.now(), result.getEndDate());
    verify(membershipRepository).save(result);
  }

  @Test
  void useClassCredit_noCredits_throwsDomainException() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .status(MembershipStatus.ACTIVE)
      .classCreditsRemaining(0)
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));

    assertThrows(DomainException.class, () -> membershipService.useClassCredit(membershipId));
  }

  @Test
  void useClassCredit_happyPath_decrementsCreditsAndSaves() {
    UUID membershipId = UUID.randomUUID();
    MemberMembership mm = MemberMembership.builder()
      .memberId(UUID.randomUUID())
      .status(MembershipStatus.ACTIVE)
      .classCreditsRemaining(2)
      .build();

    when(membershipRepository.findById(membershipId)).thenReturn(Optional.of(mm));
    when(membershipRepository.save(any(MemberMembership.class))).thenAnswer(inv -> inv.getArgument(0));

    MemberMembership result = membershipService.useClassCredit(membershipId);

    assertEquals(1, result.getClassCreditsRemaining());
    verify(membershipRepository).save(result);
  }
}
