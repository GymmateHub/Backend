package com.gymmate.organisation.internal.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new gym within an organisation.
 */
public record CreateGymRequest(
        @NotBlank(message = "Gym name is required")
        @Size(min = 2, max = 100, message = "Gym name must be between 2 and 100 characters")
        String name,
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,
        @NotBlank(message = "Contact email is required")
        @Email(message = "Contact email must be a valid email address")
        String contactEmail,
        @NotBlank(message = "Contact phone is required")
        @Size(max = 20, message = "Contact phone must not exceed 20 characters")
        String contactPhone,
        // Optional address fields
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        // Optional settings
        String timezone,
        String currency
) {
}
