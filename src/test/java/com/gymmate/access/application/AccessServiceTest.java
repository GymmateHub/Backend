package com.gymmate.access.application;

import com.gymmate.access.application.port.AccessDevicePort;
import com.gymmate.access.domain.AccessCredential;
import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.CredentialType;
import com.gymmate.access.domain.enums.DenyReason;
import com.gymmate.access.events.AccessDeniedEvent;
import com.gymmate.access.events.TailgatingSuspectedEvent;
import com.gymmate.access.infrastructure.AccessCredentialRepository;
import com.gymmate.access.infrastructure.AccessEventRepository;
import com.gymmate.access.infrastructure.AccessPointRepository;
import com.gymmate.access.infrastructure.AccessScheduleRepository;
import com.gymmate.access.infrastructure.DoorBenefitRepository;
import com.gymmate.access.infrastructure.SoftwareAccessAdapter;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.user.domain.Member;
import com.gymmate.user.infrastructure.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccessServiceTest {

  private AccessPointRepository accessPointRepository;
  private AccessCredentialRepository accessCredentialRepository;
  private AccessEventRepository accessEventRepository;
  private DoorBenefitRepository doorBenefitRepository;
  private AccessScheduleRepository accessScheduleRepository;
  private MemberRepository memberRepository;
  private MemberMembershipRepository memberMembershipRepository;
  private ApplicationEventPublisher eventPublisher;
  private AccessService service;

  private UUID gymId;
  private UUID orgId;
  private UUID memberId;
  private UUID pointId;
  private AccessPoint point;
  private AccessCredential credential;
  private Member member;

  @BeforeEach
  void setUp() {
    accessPointRepository = mock(AccessPointRepository.class);
    accessCredentialRepository = mock(AccessCredentialRepository.class);
    accessEventRepository = mock(AccessEventRepository.class);
    doorBenefitRepository = mock(DoorBenefitRepository.class);
    accessScheduleRepository = mock(AccessScheduleRepository.class);
    memberRepository = mock(MemberRepository.class);
    memberMembershipRepository = mock(MemberMembershipRepository.class);
    eventPublisher = mock(ApplicationEventPublisher.class);

    service = new AccessService(
        accessPointRepository, accessCredentialRepository, accessEventRepository,
        doorBenefitRepository, accessScheduleRepository, memberRepository,
        memberMembershipRepository, List.of(new SoftwareAccessAdapter()), eventPublisher);

    gymId = UUID.randomUUID();
    orgId = UUID.randomUUID();
    memberId = UUID.randomUUID();
    pointId = UUID.randomUUID();

    point = AccessPoint.builder().name("Main Door").mode(AccessPointMode.SOFTWARE)
        .reentryLockoutSeconds(300).build();
    point.setId(pointId);
    point.setGymId(gymId);
    point.setOrganisationId(orgId);

    credential = AccessCredential.builder().memberId(memberId).type(CredentialType.QR)
        .tokenHash("hash").build();
    credential.setId(UUID.randomUUID());
    credential.setGymId(gymId);
    credential.setActive(true);

    member = Member.builder().userId(UUID.randomUUID()).status(MemberStatus.ACTIVE)
        .waiverSigned(true).build();
    member.setId(memberId);
    member.setGymId(gymId);
    member.setOrganisationId(orgId);

    when(accessPointRepository.findById(pointId)).thenReturn(Optional.of(point));
    when(accessCredentialRepository.findByTokenHashAndActiveTrue(anyString()))
        .thenReturn(Optional.of(credential));
    when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
    when(accessEventRepository.save(any(AccessEvent.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    // No benefits/schedules configured by default
    when(doorBenefitRepository.existsByAccessPointId(pointId)).thenReturn(false);
    // Not currently inside, no recent entry
    when(accessEventRepository.findTopByMemberIdAndDecisionOrderByOccurredAtDesc(memberId, AccessDecision.GRANTED))
        .thenReturn(Optional.empty());
    when(accessEventRepository.findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
        any(), eq(AccessDecision.GRANTED), eq(AccessDirection.IN))).thenReturn(Optional.empty());
  }

  private MemberMembership activeMembership() {
    return MemberMembership.builder().memberId(memberId).build();
  }

  @Test
  void scan_grantsForActiveMemberWithActiveMembership() {
    when(memberMembershipRepository.findActiveMembershipByMemberId(memberId))
        .thenReturn(Optional.of(activeMembership()));

    AccessEvent ev = service.scan("token", pointId, AccessDirection.IN);

    assertEquals(AccessDecision.GRANTED, ev.getDecision());
    assertNull(ev.getDenyReason());
    assertFalse(ev.isTailgatingSuspected());
  }

  @Test
  void scan_deniesWhenNoActiveMembership() {
    when(memberMembershipRepository.findActiveMembershipByMemberId(memberId))
        .thenReturn(Optional.empty());

    AccessEvent ev = service.scan("token", pointId, AccessDirection.IN);

    assertEquals(AccessDecision.DENIED, ev.getDecision());
    assertEquals(DenyReason.NO_ACTIVE_MEMBERSHIP, ev.getDenyReason());
    verify(eventPublisher).publishEvent(any(AccessDeniedEvent.class));
  }

  @Test
  void scan_deniesWhenWaiverNotSigned() {
    member.setWaiverSigned(false);
    when(memberMembershipRepository.findActiveMembershipByMemberId(memberId))
        .thenReturn(Optional.of(activeMembership()));

    AccessEvent ev = service.scan("token", pointId, AccessDirection.IN);

    assertEquals(DenyReason.INCOMPLETE_SIGNUP, ev.getDenyReason());
  }

  @Test
  void scan_deniesInvalidCredential() {
    when(accessCredentialRepository.findByTokenHashAndActiveTrue(anyString()))
        .thenReturn(Optional.empty());

    AccessEvent ev = service.scan("bad", pointId, AccessDirection.IN);

    assertEquals(DenyReason.INVALID_CREDENTIAL, ev.getDenyReason());
  }

  @Test
  void scan_flagsTailgatingWhenMemberAlreadyInside() {
    when(memberMembershipRepository.findActiveMembershipByMemberId(memberId))
        .thenReturn(Optional.of(activeMembership()));
    AccessEvent insideEvent = AccessEvent.builder()
        .direction(AccessDirection.IN).decision(AccessDecision.GRANTED).build();
    when(accessEventRepository.findTopByMemberIdAndDecisionOrderByOccurredAtDesc(memberId, AccessDecision.GRANTED))
        .thenReturn(Optional.of(insideEvent));

    AccessEvent ev = service.scan("token", pointId, AccessDirection.IN);

    assertEquals(AccessDecision.DENIED, ev.getDecision());
    assertEquals(DenyReason.TAILGATING_BLOCKED, ev.getDenyReason());
    assertTrue(ev.isTailgatingSuspected());
    verify(eventPublisher).publishEvent(any(TailgatingSuspectedEvent.class));
  }

  @Test
  void scan_blocksRapidReentryWithSameCredential() {
    when(memberMembershipRepository.findActiveMembershipByMemberId(memberId))
        .thenReturn(Optional.of(activeMembership()));
    AccessEvent recentEntry = AccessEvent.builder()
        .direction(AccessDirection.IN).decision(AccessDecision.GRANTED)
        .occurredAt(LocalDateTime.now().minusSeconds(10)).build();
    when(accessEventRepository.findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
        any(), eq(AccessDecision.GRANTED), eq(AccessDirection.IN)))
        .thenReturn(Optional.of(recentEntry));

    AccessEvent ev = service.scan("token", pointId, AccessDirection.IN);

    assertEquals(DenyReason.TAILGATING_BLOCKED, ev.getDenyReason());
    assertTrue(ev.isTailgatingSuspected());
  }

  @Test
  void scan_exitAlwaysRecordedAsGranted() {
    AccessEvent ev = service.scan("token", pointId, AccessDirection.OUT);

    assertEquals(AccessDecision.GRANTED, ev.getDecision());
    assertEquals(AccessDirection.OUT, ev.getDirection());
  }

  @Test
  void handleDeviceEvent_flagsTailgatingWhenPassesExceedScans() {
    AccessEvent ev = service.handleDeviceEvent(pointId, 1, 2, "http://img/capture.jpg", null);

    assertTrue(ev.isTailgatingSuspected());
    assertEquals(2, ev.getDevicePassCount());
    assertEquals(1, ev.getValidScanCount());
    assertEquals("http://img/capture.jpg", ev.getCapturedImageUrl());
    verify(eventPublisher).publishEvent(any(TailgatingSuspectedEvent.class));
  }

  @Test
  void handleDeviceEvent_noFlagWhenScansMatchPasses() {
    AccessEvent ev = service.handleDeviceEvent(pointId, 2, 2, null, null);

    assertFalse(ev.isTailgatingSuspected());
    verify(eventPublisher, never()).publishEvent(any(TailgatingSuspectedEvent.class));
  }
}
