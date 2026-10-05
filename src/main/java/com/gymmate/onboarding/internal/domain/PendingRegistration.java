package com.gymmate.onboarding.internal.domain;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistration {

  private 
  String registrationId;

  private String email;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  @Builder.Default
  private 
  boolean emailVerified = false;

  private Instant lastOtpSentAt;

  @Builder.Default
  private 
  int otpAttempts = 0;

  private Instant createdAt;

  private Instant expiresAt;

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
