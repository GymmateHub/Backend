package com.gymmate.shared.security.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for member self-registration.
 * Uses gymSlug to identify the gym (for public registration URLs).
 */
public record MemberRegistrationRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    String lastName,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,

    @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phone,

    @NotBlank(message = "Gym slug is required")
    @Size(max = 100, message = "Gym slug must not exceed 100 characters")
    String gymSlug
) {}

