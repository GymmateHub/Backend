package com.gymmate.shared.security;

import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.service.EmailService;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

/**
 * Core authentication service.
 *
 * Handles:
 * - User authentication (login)
 * - Token management (refresh, logout, blacklist)
 * - Password reset flow
 * - OTP verification for email
 * - Password validation
 *
 * NOTE: Registration logic has been moved to dedicated services:
 * - OwnerRegistrationService
 * - MemberRegistrationService
 * - InviteService
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

    private static final int OTP_VALIDITY_MINUTES = 5;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int passwordResetExpirationMinutes;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Authenticate user and generate JWT tokens.
     * Uses Spring Security's AuthenticationManager for proper authentication.
     */
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        try {
            log.debug("Attempting authentication for user: {}", request.getEmail());

            // Fetch user first for debugging
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            log.debug("User found - ID: {}, email: {}, passwordHash exists: {}",
                user.getId(), user.getEmail(), user.getPasswordHash() != null);
            log.debug("Password verification test: {}",
                passwordService.matches(request.getPassword(), user.getPasswordHash()));

            // Use AuthenticationManager to authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Get the authenticated user details
            TenantAwareUserDetails userDetails = (TenantAwareUserDetails) authentication.getPrincipal();


            // Check if account is active (but allow INACTIVE users with unverified email to proceed)
            if (user.getStatus() != UserStatus.ACTIVE && user.getStatus() != UserStatus.INACTIVE) {
                log.warn("User with invalid status attempted to login: {}", user.getEmail());
                throw new BadCredentialsException("Account is not accessible");
            }

            // If email is not verified, send OTP and return partial response
            if (!user.isEmailVerified()) {
                log.debug("User login with unverified email: {} - Sending OTP", user.getEmail());

                // Generate and send OTP
                String userId = user.getId().toString();
                String otp = totpService.generateOtp(userId);
                totpService.updateRateLimit(userId);

                emailService.sendOtpEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    otp,
                    5 // OTP validity in minutes
                );

                log.info("OTP sent to unverified user during login: {}", user.getEmail());

                // Return response without tokens but with userId for OTP verification
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

            // Generate JWT tokens for verified users
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Update last login timestamp
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

    @Transactional
    public void initiatePasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        // Delete any existing reset tokens for this user
        resetTokenRepository.deleteByUser_Id(user.getId());

        // Create new reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.create(user, token, passwordResetExpirationMinutes);
        resetTokenRepository.save(resetToken);

        // Send reset email
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
        String oldPasswordHash = user.getPasswordHash();
        String newPasswordHash = passwordService.encode(request.getNewPassword());

        user.setPasswordHash(newPasswordHash);
        User savedUser = userRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }

    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtService.validateToken(request.getRefreshToken())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UUID userId = jwtService.extractUserId(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String newAccessToken;
        if (request.getTenantId() != null) {
            newAccessToken = jwtService.generateToken(user, request.getTenantId());
        } else {
            newAccessToken = jwtService.generateToken(user);
        }
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Logout user by blacklisting their token
     */
    @Transactional
    public void logout(String token) {
        if (token == null || token.isEmpty() || token.trim().isBlank()) {
            log.debug("No token provided for logout");
            return;
        }

        try {
            // Extract user id and expiration from the token
            // Note: We don't validate the token here because we want to blacklist it regardless
            UUID userId = jwtService.extractUserId(token);
            Date expiresAt = jwtService.extractExpiration(token);

            // Check if token is already blacklisted
            if (jwtService.isTokenBlacklisted(token)) {
                log.debug("Token is already blacklisted");
                return;
            }

            // Create and save blacklist entry
            TokenBlacklist blacklistedToken = TokenBlacklist.create(token, userId, expiresAt, "User logout");
            tokenBlacklistRepository.save(blacklistedToken);

            log.info("Token blacklisted successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Failed to logout: " + e.getMessage());
        }
    }

    // ========== Password Validation ==========

    /**
     * Validate password meets security requirements.
     * This method is used by registration services.
     */
    public void validatePassword(String password) {
        if (password == null || password.trim().length() < 8) {
            throw new DomainException("WEAK_PASSWORD",
                "Password must be at least 8 characters long");
        }

        // Add more password validation rules as needed
        if (!password.matches(".*[A-Z].*")) {
            throw new DomainException("WEAK_PASSWORD",
                "Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new DomainException("WEAK_PASSWORD",
                "Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new DomainException("WEAK_PASSWORD",
                "Password must contain at least one digit");
        }
    }

    /**
     * Send OTP to user for email verification
     */
    public RegistrationResponse sendOtpForUser(User user) {
        log.debug("Sending OTP for user: {}, emailVerified: {}", user.getEmail(), user.isEmailVerified());

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        String userId = user.getId().toString();
        log.trace("Generating OTP for userId: {}", userId);

        String otp = totpService.generateOtp(userId);
        log.trace("OTP generated for userId: {}", userId);

        totpService.updateRateLimit(userId);
        log.trace("Rate limit updated for userId: {}", userId);

        log.trace("Sending OTP email to: {} (firstName: {})", user.getEmail(), user.getFirstName());
        emailService.sendOtpEmail(
            user.getEmail(),
            user.getFirstName(),
            otp,
            OTP_VALIDITY_MINUTES
        );

        log.info("OTP email sent to user: {}", user.getEmail());

        return RegistrationResponse.builder()
            .userId(userId)
            .message("An OTP has been sent to your email for verification.")
            .expiresIn(OTP_VALIDITY_MINUTES * 60)
            .build();
    }

    /**
     * Resend OTP to user
     */
    @Transactional
    public RegistrationResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        // Atomically check and update rate limit to prevent race conditions
        if (!totpService.checkAndUpdateRateLimit(request.getUserId())) {
            long remainingSeconds = totpService.getRemainingRateLimitSeconds(request.getUserId());
            throw new BadRequestException(
                String.format("Please wait %d seconds before requesting another OTP", remainingSeconds)
            );
        }

        // Generate and send new OTP
        String otp = totpService.generateOtp(request.getUserId());

        emailService.sendOtpEmail(
            user.getEmail(),
            user.getFirstName(),
            otp,
            OTP_VALIDITY_MINUTES
        );

        log.info("OTP resent to user: {}", user.getEmail());

        return RegistrationResponse.builder()
            .userId(request.getUserId())
            .message("OTP resent to your email")
            .expiresIn(OTP_VALIDITY_MINUTES * 60)
            .retryAfter(60L)
            .build();
    }

    /**
     * Verify OTP and activate user account
     */
    @Transactional
    public VerificationTokenResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findById(UUID.fromString(request.getUserId()))
            .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified.");
        }

        // Verify OTP
        if (!totpService.verifyOtp(request.getUserId(), request.getOtp())) {
            int remainingAttempts = totpService.getRemainingAttempts(request.getUserId());
            if (remainingAttempts <= 0) {
                throw new BadRequestException("Maximum OTP attempts exceeded. Please request a new OTP.");
            }

            throw new BadRequestException(
                String.format("Invalid OTP. %d attempts remaining.", remainingAttempts)
            );
        }

        // Mark email as verified and activate user
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified and user activated for userId: {}", user.getId());

        return VerificationTokenResponse.builder()
            .verificationToken(null)
            .message("Email verified successfully. Your account is now active.")
            .expiresIn(0)
            .build();
    }
}
