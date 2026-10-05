package com.gymmate.identity.internal.application;

import com.gymmate.notification.internal.application.EmailService;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.identity.internal.application.InviteService;
import com.gymmate.identity.internal.application.MemberService;
import com.gymmate.identity.internal.application.StaffService;
import com.gymmate.identity.internal.application.TrainerService;
import com.gymmate.identity.internal.application.UserService;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.identity.internal.infrastructure.persistence.UserRepository;
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
    private com.gymmate.identity.api.spi.GymDirectory gymDirectory;
    @Mock
    private InviteService inviteService;
    @Mock
    private TotpService totpService;
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
    private com.gymmate.identity.internal.infrastructure.persistence.PasswordResetTokenRepository resetTokenRepository;
    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock
    private com.gymmate.identity.internal.infrastructure.persistence.TokenBlacklistRepository tokenBlacklistRepository;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private PasswordPolicyService passwordPolicyService;

    @InjectMocks
    private AuthenticationService authenticationService;

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
