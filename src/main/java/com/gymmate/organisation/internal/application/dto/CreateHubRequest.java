package com.gymmate.organisation.internal.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateHubRequest(
        @NotBlank(message = "Organisation name is required")
        String name,
        @NotBlank(message = "Contact email is required")
        @Email(message = "Invalid contact email")
        String contactEmail
) {
}
