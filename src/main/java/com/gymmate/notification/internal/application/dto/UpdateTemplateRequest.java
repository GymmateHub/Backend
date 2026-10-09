package com.gymmate.notification.internal.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a newsletter template.
 */
public record UpdateTemplateRequest(
        @NotBlank(message = "Template name is required")
        @Size(max = 100, message = "Template name must be less than 100 characters")
        String name,
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must be less than 255 characters")
        String subject,
        @NotBlank(message = "Body is required")
        String body,
        String placeholders
) {
}
