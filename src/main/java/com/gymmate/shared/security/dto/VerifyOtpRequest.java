package com.gymmate.shared.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

  @NotBlank(message = "Registration ID is required")
  private String registrationId;

  @NotBlank(message = "OTP is required")
  @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit number")
  private String otp;
}

