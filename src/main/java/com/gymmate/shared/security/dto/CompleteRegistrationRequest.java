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
public class CompleteRegistrationRequest {

  @NotBlank(message = "Verification token is required")
  private String verificationToken;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
      message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
  )
  private String password;

  @NotBlank(message = "Password confirmation is required")
  private String confirmPassword;
}

