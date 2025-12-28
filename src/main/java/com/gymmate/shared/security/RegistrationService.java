package com.gymmate.shared.security;

import com.gymmate.shared.domain.PendingRegistration;
import com.gymmate.shared.domain.PendingRegistrationRepository;
import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.ConflictException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.service.EmailService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.infrastructure.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

  private static final int VERIFICATION_TOKEN_EXPIRY_MINUTES = 15;
  private static final int OTP_VALIDITY_MINUTES = 5;

  private final PendingRegistrationRepository pendingRegistrationRepository;
  private final UserRepository userRepository;
  private final TotpService totpService;
  private final EmailService emailService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public RegistrationResponse initiateRegistration(InitiateRegistrationRequest request) {
    // Validate email not already registered
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ConflictException("Email is already registered");
    }

    // Check for existing pending registration
    pendingRegistrationRepository.findByEmail(request.getEmail())
        .ifPresent(existing -> {
          if (existing.isExpired()) {
            // Clean up expired registration
            pendingRegistrationRepository.delete(existing);
          } else {
            throw new ConflictException("A registration is already in progress for this email");
          }
        });

    // Create pending registration
    // Note: createdAt is auto-set by @PrePersist, expiresAt is set here (both in UTC)
    PendingRegistration pendingRegistration = PendingRegistration.builder()
        .registrationId(UUID.randomUUID().toString())
        .email(request.getEmail().toLowerCase().trim())
        .firstName(request.getFirstName().trim())
        .lastName(request.getLastName().trim())
        .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
        .expiresAt(Instant.now().plusSeconds(86400)) // 24 hours (UTC)
        .build();

    pendingRegistrationRepository.save(pendingRegistration);

    // Generate and send OTP
    String otp = totpService.generateOtp(pendingRegistration.getRegistrationId());
    totpService.updateRateLimit(pendingRegistration.getRegistrationId());

    pendingRegistration.setLastOtpSentAt(Instant.now());
    pendingRegistrationRepository.save(pendingRegistration);

    // Send OTP email
    emailService.sendOtpEmail(
        pendingRegistration.getEmail(),
        pendingRegistration.getFirstName(),
        otp,
        OTP_VALIDITY_MINUTES
    );

    log.info("Registration initiated for email: {}", request.getEmail());

    return RegistrationResponse.builder()
        .registrationId(pendingRegistration.getRegistrationId())
        .message("OTP sent to your email")
        .expiresIn(OTP_VALIDITY_MINUTES * 60)
        .build();
  }

  @Transactional
  public RegistrationResponse resendOtp(ResendOtpRequest request) {
    PendingRegistration pendingRegistration = pendingRegistrationRepository
        .findByRegistrationId(request.getRegistrationId())
        .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (pendingRegistration.isExpired()) {
      pendingRegistrationRepository.delete(pendingRegistration);
      throw new BadRequestException("Registration session has expired. Please start again.");
    }

    if (pendingRegistration.isEmailVerified()) {
      throw new BadRequestException("Email already verified. Please complete registration.");
    }

    // Check rate limit
    if (!totpService.canSendOtp(request.getRegistrationId())) {
      long remainingSeconds = totpService.getRemainingRateLimitSeconds(request.getRegistrationId());
      throw new BadRequestException(
          String.format("Please wait %d seconds before requesting another OTP", remainingSeconds)
      );
    }

    // Generate and send new OTP
    String otp = totpService.generateOtp(pendingRegistration.getRegistrationId());
    totpService.updateRateLimit(pendingRegistration.getRegistrationId());

    pendingRegistration.setLastOtpSentAt(Instant.now());
    pendingRegistrationRepository.save(pendingRegistration);

    emailService.sendOtpEmail(
        pendingRegistration.getEmail(),
        pendingRegistration.getFirstName(),
        otp,
        OTP_VALIDITY_MINUTES
    );

    log.info("OTP resent for registrationId: {}", request.getRegistrationId());

    return RegistrationResponse.builder()
        .registrationId(pendingRegistration.getRegistrationId())
        .message("OTP resent to your email")
        .expiresIn(OTP_VALIDITY_MINUTES * 60)
        .retryAfter(60L)
        .build();
  }

  @Transactional
  public VerificationTokenResponse verifyOtp(VerifyOtpRequest request) {
    PendingRegistration pendingRegistration = pendingRegistrationRepository
        .findByRegistrationId(request.getRegistrationId())
        .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (pendingRegistration.isExpired()) {
      pendingRegistrationRepository.delete(pendingRegistration);
      throw new BadRequestException("Registration session has expired. Please start again.");
    }

    if (pendingRegistration.isEmailVerified()) {
      throw new BadRequestException("Email already verified. Please complete registration.");
    }

    // Verify OTP
    if (!totpService.verifyOtp(request.getRegistrationId(), request.getOtp())) {
      pendingRegistration.incrementOtpAttempts();
      pendingRegistrationRepository.save(pendingRegistration);

      int remainingAttempts = totpService.getRemainingAttempts(request.getRegistrationId());
      if (remainingAttempts <= 0) {
        throw new BadRequestException("Maximum OTP attempts exceeded. Please request a new OTP.");
      }

      throw new BadRequestException(
          String.format("Invalid OTP. %d attempts remaining.", remainingAttempts)
      );
    }

    // Mark email as verified
    pendingRegistration.setEmailVerified(true);
    pendingRegistration.resetOtpAttempts();
    pendingRegistrationRepository.save(pendingRegistration);

    // Generate verification token (short-lived JWT)
    String verificationToken = jwtService.generateVerificationToken(
        pendingRegistration.getRegistrationId(),
        pendingRegistration.getEmail(),
        VERIFICATION_TOKEN_EXPIRY_MINUTES
    );

    log.info("Email verified for registrationId: {}", request.getRegistrationId());

    return VerificationTokenResponse.builder()
        .verificationToken(verificationToken)
        .message("Email verified successfully")
        .expiresIn(VERIFICATION_TOKEN_EXPIRY_MINUTES * 60)
        .build();
  }

  @Transactional
  public User completeRegistration(CompleteRegistrationRequest request) {
    // Validate password match
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("Passwords do not match");
    }

    // Validate and decode verification token
    Claims claims;
    try {
      claims = jwtService.validateVerificationToken(request.getVerificationToken());
    } catch (Exception e) {
      log.error("Invalid verification token", e);
      throw new BadRequestException("Verification token is invalid or expired");
    }

    String registrationId = claims.get("registrationId", String.class);

    PendingRegistration pendingRegistration = pendingRegistrationRepository
        .findByRegistrationId(registrationId)
        .orElseThrow(() -> new NotFoundException("Registration not found"));

    if (pendingRegistration.isExpired()) {
      pendingRegistrationRepository.delete(pendingRegistration);
      throw new BadRequestException("Registration session has expired. Please start again.");
    }

    if (!pendingRegistration.isEmailVerified()) {
      throw new BadRequestException("Email not verified. Please verify your email first.");
    }

    // Check if email already registered (race condition check)
    if (userRepository.existsByEmail(pendingRegistration.getEmail())) {
      throw new ConflictException("Email is already registered");
    }

    // Create user with GYM_OWNER role
    User user = User.builder()
        .email(pendingRegistration.getEmail())
        .passwordHash(passwordEncoder.encode(request.getPassword()))
        .firstName(pendingRegistration.getFirstName())
        .lastName(pendingRegistration.getLastName())
        .phone(pendingRegistration.getPhoneNumber())
        .role(UserRole.GYM_OWNER)
        .emailVerified(true)
        .build();

    user = userRepository.save(user);

    // Delete pending registration
    pendingRegistrationRepository.delete(pendingRegistration);

    // Send welcome email
    emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

    log.info("Registration completed for user: {} with role: GYM_OWNER", user.getEmail());

    return user;
  }
}

