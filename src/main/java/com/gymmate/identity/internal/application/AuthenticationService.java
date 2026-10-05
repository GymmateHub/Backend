package com.gymmate.identity.internal.application;

import com.gymmate.identity.api.dto.LoginRequest;
import com.gymmate.identity.api.dto.LoginResponse;
import com.gymmate.identity.api.dto.PasswordResetConfirmRequest;
import com.gymmate.identity.api.dto.PasswordResetRequest;
import com.gymmate.identity.api.dto.RefreshTokenRequest;
import com.gymmate.identity.api.dto.RegistrationResponse;
import com.gymmate.identity.api.dto.ResendOtpRequest;
import com.gymmate.identity.api.dto.TokenResponse;
import com.gymmate.identity.api.dto.VerificationTokenResponse;
import com.gymmate.identity.api.dto.VerifyOtpRequest;
import com.gymmate.notification.api.EmailApi;
import com.gymmate.shared.constants.AuditEventType;
import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.audit.AuditLog;
import com.gymmate.identity.internal.domain.PasswordResetToken;
import com.gymmate.identity.internal.domain.TokenBlacklist;
import com.gymmate.identity.internal.application.port.PasswordResetTokenRepository;
import com.gymmate.identity.internal.application.port.TokenBlacklistRepository;
import com.gymmate.identity.api.dto.InviteAcceptRequest;
import com.gymmate.identity.api.spi.GymDirectory;
import com.gymmate.identity.internal.application.InviteService;
import com.gymmate.identity.internal.application.MemberService;
import com.gymmate.identity.internal.application.StaffService;
import com.gymmate.identity.internal.application.TrainerService;
import com.gymmate.identity.internal.application.UserService;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.identity.api.dto.ValidateInviteResponse;
import com.gymmate.identity.internal.application.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Authentication Service handling user registration, login, logout, and token
 * management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailApi emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TotpService totpService;
    private final GymDirectory gymDirectory;
    private final InviteService inviteService;
    private final MemberService memberService;
    private final StaffService staffService;
    private final TrainerService trainerService;

    private final LoginAttemptService loginAttemptService;
    private final PasswordPolicyService passwordPolicyService;

    private static final int OTP_VALIDITY_MINUTES = 5;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int passwordResetExpirationMinutes;

    @Value("${app.frontend-url:}")
    private String frontendUrl;

    // ==================== AUTHENTICATION ====================

    @Transactional
    @AuditLog(eventType = AuditEventType.LOGIN_SUCCESS, message = "User login successful")
    public LoginResponse authenticate(LoginRequest request) {
        try {
            // Check if account is locked
            if (loginAttemptService.isAccountLocked(request.email())) {
                long remainingTime = loginAttemptService.getRemainingLockoutTime(request.email());
                throw new BadCredentialsException(
                        String.format("Account is locked. Try again in %d minutes", remainingTime));
            }

            log.debug("Attempting authentication for user: {}", request.email());

            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> {
                        loginAttemptService.loginFailed(request.email());
                        return new BadCredentialsException("User not found");
                    });

            log.debug("User found - ID: {}, email: {}", user.getId(), user.getEmail());

            // SECURITY: Spring Security's AuthenticationManager validates credentials -
            // ONLY ONCE
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            // Successful login - clear attempts
            loginAttemptService.loginSucceeded(request.email());

            if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.INACTIVE) {
                log.warn("User with invalid status attempted to login: {}", user.getEmail());
                throw new BadCredentialsException("Account is not accessible");
            }

            // If email is not verified, send OTP
            if (!user.isEmailVerified()) {
                log.debug("User login with unverified email: {} - Sending OTP", user.getEmail());

                String userId = user.getId().toString();
                String otp = totpService.generateOtp(userId);
                emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, 5, userId);

                log.info("OTP sent to unverified user during login: {}", user.getEmail());

                return LoginResponse.builder()
                        .accessToken(null)
                        .refreshToken(null)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .role(user.getRole())
                        .organisationId(user.getOrganisationId())
                        .emailVerified(false)
                        .build();
            }

            UUID defaultGymId = resolveDefaultGymId(user);
            String accessToken = defaultGymId != null
                    ? jwtService.generateToken(user, defaultGymId)
                    : jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            userService.recordLogin(user.getId());

            log.info("User authenticated successfully: {}", user.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .organisationId(user.getOrganisationId())
                    .gymId(defaultGymId)
                    .emailVerified(true)
                    .build();

        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(request.email());
            log.error("Authentication failed for user: {}", request.email(), ex);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Transactional
    @AuditLog(eventType = AuditEventType.LOGOUT)
    public void logout(String token) {
        if (token == null || token.isEmpty() || token.trim().isBlank()) {
            log.debug("No token provided for logout");
            return;
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            Date expiresAt = jwtService.extractExpiration(token);

            if (jwtService.isTokenBlacklisted(token)) {
                log.debug("Token is already blacklisted");
                return;
            }

            TokenBlacklist blacklistedToken = TokenBlacklist.create(token, userId, expiresAt, "User logout");
            tokenBlacklistRepository.save(blacklistedToken);

            log.info("Token blacklisted successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Failed to logout: " + e.getMessage());
        }
    }

    // ==================== PASSWORD RESET ====================

    @Transactional
    @AuditLog(eventType = AuditEventType.PASSWORD_RESET_REQUEST, message = "Password reset requested")
    public void initiatePasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        resetTokenRepository.deleteByUser_Id(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.create(user, token, passwordResetExpirationMinutes);
        resetTokenRepository.save(resetToken);

        String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, token);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = resetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (resetToken.isExpired()) {
            resetTokenRepository.delete(resetToken);
            throw new InvalidTokenException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordService.encode(request.getNewPassword()));
        userRepository.save(user);
        resetTokenRepository.delete(resetToken);
    }

    // ==================== TOKEN MANAGEMENT ====================

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UUID userId = jwtService.extractUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // TODO: This logic is incomplete. the existing refresh token should be
        // blacklisted upon generating a new one.
        String newAccessToken = request.getTenantId() != null
                ? jwtService.generateToken(user, request.getTenantId())
                : jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // ==================== USER REGISTRATION ====================

    @Transactional
    public LoginResponse acceptInvite(InviteAcceptRequest request) {
        log.info("Accepting invite with token: {}", request.inviteToken());

        ValidateInviteResponse validated = inviteService.validateInvite(request.inviteToken());

        if (validated.expired()) {
            throw new InvalidTokenException("Invite has expired");
        }

        validatePassword(request.password());

        if (userRepository.existsByEmail(validated.email())) {
            throw new DomainException("USER_ALREADY_EXISTS", "User with this email already exists");
        }

        // Mark invite as accepted
        inviteService.acceptInvite(request.inviteToken());

        // Create User
        User user = User.builder()
                .email(validated.email())
                .firstName(request.firstName() != null ? request.firstName() : validated.firstName())
                .lastName(request.lastName() != null ? request.lastName() : validated.lastName())
                .phone(request.phone())
                .passwordHash(passwordService.encode(request.password()))
                .role(validated.role())
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        user.setOrganisationId(validated.organisationId());

        user = userRepository.save(user);

        // BUG-003: acceptInvite() previously only created the User row, leaving no
        // Member/Staff/Trainer domain entity — every subsequent /api/members/me,
        // /api/staff, /api/trainers call for this user then 404'd. Create the matching
        // profile now, with sensible defaults for fields the invite flow doesn't
        // collect.
        switch (user.getRole()) {
            case MEMBER -> memberService.createMember(user.getId(), validated.gymId(), null);
            case STAFF -> staffService.createStaff(user.getId(), "Staff", "General",
                    null, java.time.LocalDate.now(), "full_time");
            case TRAINER -> trainerService.createTrainer(user.getId(), new String[0], null,
                    null, null, java.time.LocalDate.now(), "full_time");
            default -> {
                // ADMIN and other org-level roles have no separate domain profile.
            }
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .organisationId(user.getOrganisationId())
                .emailVerified(true)
                .build();
    }

    // ==================== OTP VERIFICATION ====================

    public RegistrationResponse sendOtpForUser(User user) {
        log.debug("Sending OTP for user: {}", user.getEmail());

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        String userId = user.getId().toString();
        String otp = totpService.generateOtp(userId);

        emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, OTP_VALIDITY_MINUTES, userId);
        log.info("OTP email sent to user: {}", user.getEmail());

        return RegistrationResponse.builder()
                .userId(userId)
                .message("An OTP has been sent to your email for verification.")
                .expiresIn(OTP_VALIDITY_MINUTES * 60)
                .build();
    }

    @Transactional
    public RegistrationResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        if (!totpService.checkAndUpdateRateLimit(request.getUserId())) {
            long remainingSeconds = totpService.getRemainingRateLimitSeconds(request.getUserId());
            throw new BadRequestException(
                    String.format("Please wait %d seconds before requesting another OTP", remainingSeconds));
        }

        String otp = totpService.generateOtp(request.getUserId());
        emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, OTP_VALIDITY_MINUTES, request.getUserId());

        log.info("OTP resent to user: {}", user.getEmail());

        return RegistrationResponse.builder()
                .userId(request.getUserId())
                .message("OTP resent to your email")
                .expiresIn(OTP_VALIDITY_MINUTES * 60)
                .retryAfter(60L)
                .build();
    }

    @Transactional
    public VerificationTokenResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        if (!totpService.verifyOtp(request.getUserId(), request.getOtp())) {
            int remainingAttempts = totpService.getRemainingAttempts(request.getUserId());
            if (remainingAttempts <= 0) {
                throw new BadRequestException("Maximum OTP attempts exceeded. Please request a new OTP.");
            }
            throw new BadRequestException(String.format("Invalid OTP. %d attempts remaining.", remainingAttempts));
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified and user activated for userId: {}", user.getId());

        // BUG-002: issue tokens on verify so the user is immediately logged in, same as
        // acceptInvite().
        UUID defaultGymId = resolveDefaultGymId(user);
        String accessToken = defaultGymId != null
                ? jwtService.generateToken(user, defaultGymId)
                : jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return VerificationTokenResponse.builder()
                .verificationToken(null)
                .message("Email verified successfully. Your account is now active.")
                .expiresIn(0)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .organisationId(user.getOrganisationId())
                .gymId(defaultGymId)
                .build();
    }

    // Update password change methods to check history
    @AuditLog(eventType = AuditEventType.PASSWORD_CHANGE, message = "Password changed successfully")
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Verify old password
        if (!passwordService.matches(oldPassword, user.getPasswordHash())) {
            // DomainException (400), not BadCredentialsException (401): a wrong current password
            // must not look like an expired session to the client.
            throw new DomainException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }

        // Validate new password
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword(newPassword,
                userId);

        if (!result.valid()) {
            throw new DomainException("WEAK_PASSWORD",
                    "Password does not meet security requirements: " + String.join(", ", result.errors()));
        }

        // Update password
        String newHashedPassword = passwordService.encode(newPassword);
        user.setPasswordHash(newHashedPassword);
        userRepository.save(user);

        // Add to password history
        passwordPolicyService.addToPasswordHistory(userId, newHashedPassword);
    }

    // ==================== PRIVATE HELPERS ====================

    /** Enforces the password policy for a new account; package-visible for the identity facade. */
    void validatePassword(String password) {
        PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword(password, null);

        if (!result.valid()) {
            throw new DomainException("WEAK_PASSWORD",
                    "Password does not meet security requirements: " + String.join(", ", result.errors()));
        }
    }

    private UUID resolveDefaultGymId(User user) {
        if (user == null || user.getOrganisationId() == null) {
            return null;
        }
        try {
            return gymDirectory.findDefaultActiveGymId(user.getOrganisationId()).orElse(null);
        } catch (Exception e) {
            log.debug("Could not resolve active gym for user {}: {}", user.getEmail(), e.getMessage());
        }
        return null;
    }

}
