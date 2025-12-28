package com.gymmate.shared.security.dto;

import jakarta.validation.constraints.Email;
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
public class InitiateRegistrationRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  private String email;

  @NotBlank(message = "First name is required")
  @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
  private String lastName;

  @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid")
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  private String phoneNumber;
}

