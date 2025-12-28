package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_registrations", indexes = {
    @Index(name = "idx_pending_reg_email", columnList = "email"),
    @Index(name = "idx_pending_reg_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration {

  @Id
  @Column(name = "registration_id", nullable = false, length = 36)
  private String registrationId;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Column(name = "email_verified", nullable = false)
  @Builder.Default
  private boolean emailVerified = false;

  @Column(name = "last_otp_sent_at")
  private Instant lastOtpSentAt;

  @Column(name = "otp_attempts", nullable = false)
  @Builder.Default
  private int otpAttempts = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @PrePersist
  protected void onCreate() {
    if (registrationId == null) {
      registrationId = UUID.randomUUID().toString();
    }
    if (createdAt == null) {
      createdAt = Instant.now();
    }
    if (expiresAt == null) {
      // Expires in 24 hours
      expiresAt = createdAt.plusSeconds(86400);
    }
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public void incrementOtpAttempts() {
    this.otpAttempts++;
  }

  public void resetOtpAttempts() {
    this.otpAttempts = 0;
  }
}

