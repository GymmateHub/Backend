package com.gymmate.shared.security.service;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.organisation.application.OrganisationService;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;

import com.gymmate.shared.security.domain.PasswordResetToken;
import com.gymmate.shared.security.domain.TokenBlacklist;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.security.repository.PasswordResetTokenRepository;
import com.gymmate.shared.security.repository.TokenBlacklistRepository;
import com.gymmate.shared.service.EmailService;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
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
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TotpService totpService;
    private final OrganisationService organisationService;
    private final GymRepository gymRepository;

    private static final int OTP_VALIDITY_MINUTES = 5;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int passwordResetExpirationMinutes;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ==================== AUTHENTICATION ====================

    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        try {
            log.debug("Attempting authentication for user: {}", request.getEmail());

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            log.debug("User found - ID: {}, email: {}", user.getId(), user.getEmail());

            // Spring Security's AuthenticationManager validates credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.INACTIVE) {
                log.warn("User with invalid status attempted to login: {}", user.getEmail());
                throw new BadCredentialsException("Account is not accessible");
            }

            // If email is not verified, send OTP
            if (!user.isEmailVerified()) {
                log.debug("User login with unverified email: {} - Sending OTP", user.getEmail());

                String userId = user.getId().toString();
                String otp = totpService.generateOtp(userId);
                emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, 5);

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

            user.updateLastLogin();
            userRepository.save(user);

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
            log.error("Authentication failed for user: {}", request.getEmail(), ex);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    // ==================== PASSWORD RESET ====================

    @Transactional
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

    @Transactional
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

    // ==================== USER REGISTRATION ====================

    @Transactional
    public User registerUser(String email, String firstName, String lastName,
            String plainPassword, String phone, UserRole role) {
        if (role == UserRole.OWNER) {
            throw new DomainException("INVALID_REGISTRATION", "OWNER registration must use the OTP verification flow");
        }

        UUID currentOrganisationId = TenantContext.getCurrentTenantId();
        if (currentOrganisationId != null
                && userRepository.findByEmailAndOrganisationId(email, currentOrganisationId).isPresent()) {
            throw new DomainException("USER_ALREADY_EXISTS",
                    "A user with email '" + email + "' already exists in this organisation");
        }

        if (role == UserRole.ADMIN && userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS", "A user with email '" + email + "' already exists");
        }

        validatePassword(plainPassword);

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordService.encode(plainPassword))
                .phone(phone)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User registerMember(String email, String firstName, String lastName,
            String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION", "Members can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.MEMBER);
    }

    @Transactional
    public User registerGymAdmin(String email, String firstName, String lastName,
            String plainPassword, String phone) {
        log.info("Registering gym admin with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS", "A user with email '" + email + "' already exists");
        }

        validatePassword(plainPassword);

        String organisationName = firstName + "'s Gym Organization";
        String slug = organisationService.generateSlug(organisationName);
        Organisation organisation = organisationService.createOrganisation(organisationName, slug, email);

        log.info("Organisation created: {} (ID: {})", organisation.getName(), organisation.getId());

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordService.encode(plainPassword))
                .phone(phone)
                .organisationId(organisation.getId())
                .role(UserRole.OWNER)
                .status(UserStatus.INACTIVE)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User saved - ID: {}, email: {}", savedUser.getId(), savedUser.getEmail());

        organisationService.assignOwner(organisation.getId(), savedUser.getId());
        log.info("Organisation owner assigned");

        Gym defaultGym = Gym.createDefault(organisationName, email, phone != null ? phone : "", organisation.getId());
        Gym savedGym = gymRepository.save(defaultGym);
        log.info("Default gym created: {} (ID: {})", savedGym.getName(), savedGym.getId());

        return savedUser;
    }

    @Transactional
    public User registerTrainer(String email, String firstName, String lastName,
            String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION", "Trainers can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.TRAINER);
    }

    @Transactional
    public User registerStaff(String email, String firstName, String lastName,
            String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION", "Staff can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.STAFF);
    }

    // ==================== OTP VERIFICATION ====================

    public RegistrationResponse sendOtpForUser(User user) {
        log.debug("Sending OTP for user: {}", user.getEmail());

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        String userId = user.getId().toString();
        String otp = totpService.generateOtp(userId);

        emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, OTP_VALIDITY_MINUTES);
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
        emailService.sendOtpEmail(user.getEmail(), user.getFirstName(), otp, OTP_VALIDITY_MINUTES);

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

    // ==================== PRIVATE HELPERS ====================

    private void validatePassword(String password) {
        if (password == null || password.trim().length() < 8) {
            throw new DomainException("WEAK_PASSWORD", "Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new DomainException("WEAK_PASSWORD", "Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new DomainException("WEAK_PASSWORD", "Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new DomainException("WEAK_PASSWORD", "Password must contain at least one digit");
        }
    }
}
