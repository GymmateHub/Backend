package com.gymmate.shared.security.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for owner (gym admin) registration.
 * Creates organisation, user, and default gym in a single transaction.
 */
public record OwnerRegistrationRequest(
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

    @NotBlank(message = "Organisation name is required")
    @Size(min = 2, max = 255, message = "Organisation name must be between 2 and 255 characters")
    String organisationName,

    @NotBlank(message = "Gym name is required")
    @Size(min = 2, max = 255, message = "Gym name must be between 2 and 255 characters")
    String gymName,

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    String timezone,

    @Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code")
    String country
) {}

