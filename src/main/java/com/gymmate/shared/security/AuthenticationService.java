package com.gymmate.shared.security;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.dto.*;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int passwordResetExpirationMinutes;

    /**
     * Authenticate user and generate JWT tokens.
     * Uses Spring Security's AuthenticationManager for proper authentication.
     */
    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        try {
            // Use AuthenticationManager to authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Get the authenticated user details
            TenantAwareUserDetails userDetails = (TenantAwareUserDetails) authentication.getPrincipal();

            // Fetch the full user entity
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Check if account is active
            if (!user.isActive()) {
                log.warn("Inactive user attempted to login: {}", user.getEmail());
                throw new BadCredentialsException("Account is not active");
            }

            // Generate JWT tokens
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
                    .gymId(user.getGymId())
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
        String resetLink = String.format("%s/reset-password?token=%s", "https://your-frontend-url", token);
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
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

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

    // ========== Registration Methods ==========

    /**
     * Register a new user in the system.
     */
    @Transactional
    public User registerUser(String email, String firstName, String lastName,
                           String plainPassword, String phone, UserRole role) {
        // Check if user already exists in current gym context
        UUID currentGymId = TenantContext.getCurrentTenantId();
        if (currentGymId != null && userRepository.findByEmailAndGymId(email, currentGymId).isPresent()) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists in this gym");
        }

        // For ADMIN role (gym owners/admins), check system-wide unique email
        if (role == UserRole.ADMIN && userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists");
        }

        // Validate password requirements
        validatePassword(plainPassword);

        // Encode password
        String passwordHash = passwordService.encode(plainPassword);

        // Create new user using builder
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Register a new gym member.
     */
    @Transactional
    public User registerMember(String email, String firstName, String lastName,
                             String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Members can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.MEMBER);
    }

    /**
     * Register a new gym admin/owner.
     */
    @Transactional
    public User registerGymAdmin(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        // Check if user already exists (system-wide)
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists");
        }

        // Validate password requirements
        validatePassword(plainPassword);

        // Encode password
        String passwordHash = passwordService.encode(plainPassword);

        // Create new gym admin
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        // Gym admins can be created with or without gym context
        // If within gym context, they'll be assigned to that gym
        // If not, gymId will be set when they create/join a gym

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Register a new trainer.
     */
    @Transactional
    public User registerTrainer(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Trainers can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.TRAINER);
    }

    /**
     * Register a new staff member.
     */
    @Transactional
    public User registerStaff(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Staff can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.STAFF);
    }

    /**
     * Validate password meets security requirements.
     */
    private void validatePassword(String password) {
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
}
