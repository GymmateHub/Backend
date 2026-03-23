package com.gymmate.shared.security.service;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.notification.application.EmailService;
import com.gymmate.organisation.application.OrganisationService;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.shared.constants.AuditEventType;
import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.aspect.AuditLog;
import com.gymmate.shared.security.domain.PasswordResetToken;
import com.gymmate.shared.security.domain.TokenBlacklist;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.security.repository.PasswordResetTokenRepository;
import com.gymmate.shared.security.repository.TokenBlacklistRepository;
import com.gymmate.user.api.dto.InviteAcceptRequest;
import com.gymmate.user.api.dto.MemberRegistrationRequest;
import com.gymmate.user.api.dto.OwnerRegistrationRequest;
import com.gymmate.user.application.InviteService;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.user.api.dto.ValidateInviteResponse;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TotpService totpService;
    private final OrganisationService organisationService;
    private final GymService gymService;
    private final InviteService inviteService;

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

            // SECURITY: Spring Security's AuthenticationManager validates credentials - ONLY ONCE
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

            String accessToken = jwtService.generateToken(user);
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
    public User registerOwner(OwnerRegistrationRequest request) {
        log.info("Registering owner: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException("USER_ALREADY_EXISTS",
                    "A user with email '" + request.email() + "' already exists");
        }

        validatePassword(request.password());

        // 1. Create User (Inactive, waiting for OTP)
        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordService.encode(request.password()))
                .phone(request.phone())
                .role(UserRole.GYM_OWNER)
                .status(UserStatus.INACTIVE) // Requires OTP verification
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // 2. Create Organisation & Hub (Atomic transaction)
        // createHub creates Organisation, Subscription, and links owner
        Organisation organisation = organisationService.createHub(request.organisationName(), request.email(), user);

        // 3. Create initial Gym
        // We create it manually to bypass the active-owner check in
        // GymService.registerGym
        Gym gym = new Gym(request.gymName(), "Main Gym", request.email(), request.phone(), user.getId());
        gym.setOrganisationId(organisation.getId());
        gym.setTimezone(request.timezone());
        gym.updateAddress(null, null, null, request.country(), null);

        gymService.saveGym(gym);

        return user;
    }

    @Transactional
    public User registerMember(MemberRegistrationRequest request) {
        log.info("Registering member: {}", request.email());

        // Resolve gym if slug provided
        UUID organisationId = null;

        if (request.gymSlug() != null) {
            Organisation org = organisationService.getBySlug(request.gymSlug());
            organisationId = org.getId();
            // Assign to first gym of the org for now, or find gym by slug if gyms had
            // slugs?
            // The request says "gymSlug". OrganisationService has `getBySlug`.
            // But gyms might not have slugs yet?
            // Assuming `gymSlug` maps to Organisation Slug as per `createHub`.
            // Members register to a Gym.
            // "URL pattern: app.gymmatehub.com/join/:gymSlug"
            // "Resolves gymId server-side from public URL"
            // If the slug is for Gym, we need GymService.getBySlug?
            // GymService checks `organisationService.generateSlug`.
            // Let's assume for now `gymSlug` in request refers to Organisation Slug (as
            // gyms are often one per org initially).
            // Or we need to implement Gym Slugs.
            // For now, let's look up Organisation by slug, and pick the first active gym.
            List<Gym> gyms = gymService.getActiveGymsByOrganisation(organisationId);
            if (gyms.isEmpty()) {
                throw new DomainException("NO_ACTIVE_GYM", "No active gym find for this link");
            }
            // gymId = gyms.get(0).getId(); // Unused
        } else {
            throw new DomainException("INVALID_REQUEST", "Gym slug is required for public registration");
        }

        if (userRepository.existsByEmail(request.email())) {
            // For members, checking global uniqueness or per-org?
            // "Multi-tenant". User table is global.
            // If user exists, they should login?
            throw new DomainException("USER_ALREADY_EXISTS", "User already exists. Please login.");
        }

        validatePassword(request.password());

        User user = User.builder()
                .email(request.email())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .passwordHash(passwordService.encode(request.password()))
                .phone(request.phone())
                .role(UserRole.MEMBER)
                .status(UserStatus.INACTIVE)
                .emailVerified(false)
                .build();
        user.setOrganisationId(organisationId); // Associate with org (inherited from TenantEntity)

        // We also need to create Member entity?
        // GymService/MemberService should handle that.
        // But `AuthenticationService` registers the USER.
        // Member creation (in `members` table) happens after?
        // "Step 4 — Membership Selection... POST /api/member-memberships"
        // So here we only create the User.

        return userRepository.save(user);
    }

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

        return VerificationTokenResponse.builder()
                .verificationToken(null)
                .message("Email verified successfully. Your account is now active.")
                .expiresIn(0)
                .build();
    }

    // Update password change methods to check history
    @AuditLog(eventType = AuditEventType.PASSWORD_CHANGE, message = "Password changed successfully")
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
      User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

      // Verify old password
      if (!passwordService.matches(oldPassword, user.getPasswordHash())) {
        throw new BadCredentialsException("Current password is incorrect");
      }

      // Validate new password
      PasswordPolicyService.PasswordValidationResult result =
        passwordPolicyService.validatePassword(newPassword, userId);

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

  private void validatePassword(String password) {
    PasswordPolicyService.PasswordValidationResult result = passwordPolicyService.validatePassword(password, null);

    if (!result.valid()) {
      throw new DomainException("WEAK_PASSWORD",
        "Password does not meet security requirements: " + String.join(", ", result.errors()));
    }
  }
}
