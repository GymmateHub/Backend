package com.gymmate.shared.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {

  private String userId;
  private String message;
  private int expiresIn; // seconds
  private Long retryAfter; // seconds (for rate limiting)
}

