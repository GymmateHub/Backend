package com.gymmate.shared.security;

import com.gymmate.shared.exception.AuthException;
import com.gymmate.shared.exception.InvalidTokenException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.service.EmailService;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;

    @Value("${app.password-reset.expiration-minutes:30}")
    private int passwordResetExpirationMinutes;

    @Transactional
    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new AuthException("Account is not active");
        }

        String accessToken = jwtTokenProvider.generateToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        user.updateLastLogin();
        userRepository.save(user);

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
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        String newAccessToken;
        if (request.getTenantId() != null) {
            newAccessToken = jwtTokenProvider.generateToken(user, request.getTenantId());
        } else {
            newAccessToken = jwtTokenProvider.generateToken(user);
        }
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        // You could implement a token blacklist here if needed
        // For now, client-side token removal is sufficient as tokens are short-lived
    }
}
