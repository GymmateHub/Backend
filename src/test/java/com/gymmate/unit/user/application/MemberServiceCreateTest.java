package com.gymmate.unit.user.application;

import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceCreateTest {

    @Mock private MemberRepository memberRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private MemberService memberService;

    private final UUID orgId = UUID.randomUUID();
    private final UUID gymId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenantId(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private void stubNewUserAndSave() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsMemberScopedToRequestedGymAndOrganisation() {
        stubNewUserAndSave();

        Member member = memberService.createMemberWithDetails(
                "jane@example.com", "Jane", "Doe", "+2348000000000", gymId, null);

        assertEquals(gymId, member.getGymId());
        assertEquals(orgId, member.getOrganisationId());
        assertEquals(MemberStatus.ACTIVE, member.getStatus());
        assertTrue(member.getMembershipNumber().startsWith("GM-"));
    }

    @Test
    void fallsBackToGymFromTenantContextWhenRequestOmitsIt() {
        TenantContext.setCurrentGymId(gymId);
        stubNewUserAndSave();

        Member member = memberService.createMemberWithDetails(
                "jane@example.com", "Jane", "Doe", null, null, null);

        assertEquals(gymId, member.getGymId());
    }

    @Test
    void rejectsMissingGymWithBadRequestInsteadOfFailingOnNotNullConstraint() {
        DomainException ex = assertThrows(DomainException.class, () ->
                memberService.createMemberWithDetails("jane@example.com", "Jane", "Doe", null, null, null));

        assertEquals("GYM_ID_REQUIRED", ex.getErrorCode());
        verify(memberRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsEmailBelongingToAnotherOrganisation() {
        User foreign = User.builder().email("jane@example.com").build();
        foreign.setId(UUID.randomUUID());
        foreign.setOrganisationId(UUID.randomUUID());
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(foreign));

        DomainException ex = assertThrows(DomainException.class, () ->
                memberService.createMemberWithDetails("jane@example.com", "Jane", "Doe", null, gymId, null));

        assertEquals("EMAIL_IN_USE", ex.getErrorCode());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void returnsExistingMemberProfileInsteadOfDuplicating() {
        User existing = User.builder().email("jane@example.com").build();
        existing.setId(UUID.randomUUID());
        existing.setOrganisationId(orgId);
        Member profile = Member.builder().userId(existing.getId()).build();
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existing));
        when(memberRepository.findByUserId(existing.getId())).thenReturn(Optional.of(profile));

        Member result = memberService.createMemberWithDetails("jane@example.com", "Jane", "Doe", null, gymId, null);

        assertSame(profile, result);
        verify(memberRepository, never()).save(any());
    }

    @Test
    void requiresEmailAndNames() {
        assertEquals("EMAIL_REQUIRED", assertThrows(DomainException.class, () ->
                memberService.createMemberWithDetails(" ", "Jane", "Doe", null, gymId, null)).getErrorCode());
        assertEquals("FIRST_NAME_REQUIRED", assertThrows(DomainException.class, () ->
                memberService.createMemberWithDetails("a@b.co", "", "Doe", null, gymId, null)).getErrorCode());
        assertEquals("LAST_NAME_REQUIRED", assertThrows(DomainException.class, () ->
                memberService.createMemberWithDetails("a@b.co", "Jane", null, null, gymId, null)).getErrorCode());
    }
}
