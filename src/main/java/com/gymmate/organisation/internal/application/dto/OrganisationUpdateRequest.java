package com.gymmate.organisation.internal.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating organisation details.
 */
public record OrganisationUpdateRequest(
        @Size(min = 2, max = 100, message = "Organisation name must be between 2 and 100 characters")
        String name,
        @Email(message = "Contact email must be a valid email address")
        String contactEmail,
        @Size(max = 20, message = "Contact phone must not exceed 20 characters")
        String contactPhone,
        @Email(message = "Billing email must be a valid email address")
        String billingEmail,
        // Settings can be a JSON string for flexibility
        String settings
) {
}
