package com.gymmate.shared.security.dto;

import com.gymmate.shared.constants.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationTokenResponse {

  private String verificationToken;
  private String message;
  private int expiresIn; // seconds

  // BUG-002: verifyOtp() previously left the user verified-but-signed-out, forcing a second
  // separate login call. These mirror LoginResponse so the client can log the user straight in.
  private String accessToken;
  private String refreshToken;
  private UUID userId;
  private String email;
  private UserRole role;
  private UUID organisationId;
}

