package com.gymmate.shared.security.service;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.notification.application.EmailService;
import com.gymmate.organisation.application.OrganisationLimitService;
import com.gymmate.organisation.application.OrganisationService;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.user.api.dto.MemberRegistrationRequest;
import com.gymmate.user.api.dto.OwnerRegistrationRequest;
import com.gymmate.user.application.InviteService;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.application.StaffService;
import com.gymmate.user.application.TrainerService;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordService passwordService;
    @Mock
    private JwtService jwtService; // Needed for completeness of mock injection
    @Mock
    private EmailService emailService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private GymService gymService;
    @Mock
    private InviteService inviteService;
    @Mock
    private TotpService totpService;
    @Mock
    private OrganisationLimitService organisationLimitService;
    @Mock
    private MemberService memberService;
    @Mock
    private StaffService staffService;
    @Mock
    private TrainerService trainerService;

    // We need all dependencies for @InjectMocks to work if constructor injection is
    // used (which it is)
    // However, Mockito is smart enough to inject mocks by type.
    // If some are missing (like repositories), it might fail if they are required
    // in the constructor and not mocked.
    // Let's add the rest just in case or rely on lenient mocks if not used.
    @Mock
    private com.gymmate.shared.security.repository.PasswordResetTokenRepository resetTokenRepository;
    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock
    private com.gymmate.shared.security.repository.TokenBlacklistRepository tokenBlacklistRepository;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private PasswordPolicyService passwordPolicyService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRegisterOwner() {
        OwnerRegistrationRequest request = new OwnerRegistrationRequest(
                "owner@example.com", "Owner", "User", "Password123!", "1234567890", "My Org", "My Gym", "UTC", "US");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(passwordPolicyService.validatePassword(anyString(), any()))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(true, List.of()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        Organisation mockOrg = Organisation.builder().build();
        mockOrg.setId(UUID.randomUUID());
        when(organisationService.createHub(anyString(), anyString(), any(User.class))).thenReturn(mockOrg);

        User result = authenticationService.registerOwner(request);

        assertNotNull(result);
        assertEquals(UserRole.GYM_OWNER, result.getRole());
        verify(userRepository).save(any(User.class));
        verify(organisationService).createHub(eq("My Org"), eq("owner@example.com"), any(User.class));
        verify(gymService).saveGym(any(Gym.class));
    }

    @Test
    void shouldRegisterOwnerWithNullOptionalFields() {
        OwnerRegistrationRequest request = new OwnerRegistrationRequest(
                "owner@example.com", "Owner", "User", "Password123!", "1234567890", null, null, null, null);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(passwordPolicyService.validatePassword(anyString(), any()))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(true, List.of()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        Organisation mockOrg = Organisation.builder().build();
        mockOrg.setId(UUID.randomUUID());
        when(organisationService.createHub(anyString(), anyString(), any(User.class))).thenReturn(mockOrg);

        User result = authenticationService.registerOwner(request);

        assertNotNull(result);
        assertEquals(UserRole.GYM_OWNER, result.getRole());
        verify(organisationService).createHub(eq("Owner's Organisation"), eq("owner@example.com"), any(User.class));
        verify(gymService).saveGym(any(Gym.class));
    }

    @Test
    void shouldThrowExceptionIfOwnerExists() {
        OwnerRegistrationRequest request = new OwnerRegistrationRequest(
                "owner@example.com", "Owner", "User", "Password123!", "1234567890", "My Org", "My Gym", "UTC", "US");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class, () -> {
            authenticationService.registerOwner(request);
        });

        assertEquals("USER_ALREADY_EXISTS", exception.getErrorCode());
    }

    @Test
    void shouldRegisterMember() {
        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "member@example.com", "Member", "User", "Password123!", "1234567890", "gym-slug");

        Organisation mockOrg = Organisation.builder().build();
        mockOrg.setId(UUID.randomUUID());
        when(organisationService.getBySlug("gym-slug")).thenReturn(mockOrg);

        Gym mockGym = Gym.builder().build();
        mockGym.setId(UUID.randomUUID());
        when(gymService.getActiveGymsByOrganisation(mockOrg.getId())).thenReturn(List.of(mockGym));

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(passwordPolicyService.validatePassword(anyString(), any()))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(true, List.of()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        User result = authenticationService.registerMember(request);

        assertNotNull(result);
        assertEquals(UserRole.MEMBER, result.getRole());
        assertEquals(mockOrg.getId(), result.getOrganisationId());
    }

    @Test
    void shouldThrowExceptionForMemberWithoutGymSlug() {
        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "member@example.com", "Member", "User", "Password123!", "1234567890", null);

        DomainException exception = assertThrows(DomainException.class, () -> {
            authenticationService.registerMember(request);
        });

        assertEquals("INVALID_REQUEST", exception.getErrorCode());
    }
    // ---- currency defaulting at registration ----

    private Gym registerOwnerWithCountry(String country) {
        OwnerRegistrationRequest request = new OwnerRegistrationRequest(
                "owner@example.com", "Owner", "User", "Password123!", "1234567890", "My Org", "My Gym", "UTC", country);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(passwordPolicyService.validatePassword(anyString(), any()))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(true, List.of()));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        Organisation mockOrg = Organisation.builder().build();
        mockOrg.setId(UUID.randomUUID());
        when(organisationService.createHub(anyString(), anyString(), any(User.class))).thenReturn(mockOrg);

        authenticationService.registerOwner(request);

        org.mockito.ArgumentCaptor<Gym> captor = org.mockito.ArgumentCaptor.forClass(Gym.class);
        verify(gymService).saveGym(captor.capture());
        return captor.getValue();
    }

    @Test
    void registerOwner_nigerianGymGetsNgnCurrency() {
        assertEquals("NGN", registerOwnerWithCountry("Nigeria").getCurrency());
    }

    @Test
    void registerOwner_missingCountryDefaultsToPlatformCurrency() {
        assertEquals("NGN", registerOwnerWithCountry(null).getCurrency());
    }

    @Test
    void registerOwner_usGymKeepsUsd() {
        assertEquals("USD", registerOwnerWithCountry("US").getCurrency());
    }

    // ---- authenticated change password ----

    private User userWithPassword(UUID id) {
        User user = User.builder().email("owner@example.com").passwordHash("old-hash").build();
        user.setId(id);
        return user;
    }

    @Test
    void changePassword_updatesHashAndHistory() {
        UUID id = UUID.randomUUID();
        User user = userWithPassword(id);
        when(userRepository.findById(id)).thenReturn(java.util.Optional.of(user));
        when(passwordService.matches("OldPassword123!", "old-hash")).thenReturn(true);
        when(passwordPolicyService.validatePassword("NewPassword123!", id))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(true, List.of()));
        when(passwordService.encode("NewPassword123!")).thenReturn("new-hash");

        authenticationService.changePassword(id, "OldPassword123!", "NewPassword123!");

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
        verify(passwordPolicyService).addToPasswordHistory(id, "new-hash");
    }

    @Test
    void changePassword_wrongCurrentPasswordIsBadRequestNotUnauthorized() {
        UUID id = UUID.randomUUID();
        User user = userWithPassword(id);
        when(userRepository.findById(id)).thenReturn(java.util.Optional.of(user));
        when(passwordService.matches("wrong", "old-hash")).thenReturn(false);

        DomainException ex = assertThrows(DomainException.class,
                () -> authenticationService.changePassword(id, "wrong", "NewPassword123!"));

        assertEquals("INVALID_CURRENT_PASSWORD", ex.getErrorCode());
        assertEquals("old-hash", user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_rejectsPasswordThatFailsPolicy() {
        UUID id = UUID.randomUUID();
        User user = userWithPassword(id);
        when(userRepository.findById(id)).thenReturn(java.util.Optional.of(user));
        when(passwordService.matches("OldPassword123!", "old-hash")).thenReturn(true);
        when(passwordPolicyService.validatePassword("short", id))
                .thenReturn(new PasswordPolicyService.PasswordValidationResult(false, List.of("too short")));

        DomainException ex = assertThrows(DomainException.class,
                () -> authenticationService.changePassword(id, "OldPassword123!", "short"));

        assertEquals("WEAK_PASSWORD", ex.getErrorCode());
        assertEquals("old-hash", user.getPasswordHash());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordPolicyService, never()).addToPasswordHistory(any(), anyString());
    }
}
