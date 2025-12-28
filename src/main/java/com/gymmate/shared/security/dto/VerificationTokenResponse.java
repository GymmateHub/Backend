package com.gymmate.shared.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationTokenResponse {

  private String verificationToken;
  private String message;
  private int expiresIn; // seconds
}

