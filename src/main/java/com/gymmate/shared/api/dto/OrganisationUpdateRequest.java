package com.gymmate.shared.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating organisation details.
 */
@Data
public class OrganisationUpdateRequest {

    @Size(min = 2, max = 100, message = "Organisation name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    @Email(message = "Billing email must be a valid email address")
    private String billingEmail;

    // Settings can be a JSON string for flexibility
    private String settings;
}

