package com.gymmate.notification.internal.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating a newsletter template.
 */
public record CreateTemplateRequest(
        UUID gymId,
        @NotBlank(message = "Template name is required")
        @Size(max = 100, message = "Template name must be less than 100 characters")
        String name,
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must be less than 255 characters")
        String subject,
        @NotBlank(message = "Body is required")
        String body,
        String templateType,
        String placeholders
) {
    public CreateTemplateRequest {
        if (templateType == null) templateType = "EMAIL";
        if (placeholders == null) placeholders = "[]";
    }
}
