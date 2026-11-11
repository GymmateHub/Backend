package com.gymmate.configuration.internal.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating a platform-default or organisation-override message template.
 */
public record UpdateMessageTemplateRequest(
        @Size(max = 150, message = "Template name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Subject or title is required")
        @Size(max = 300, message = "Subject or title must not exceed 300 characters")
        String subjectOrTitle,

        String bodyTemplate,

        Boolean bodyCustomised,

        Boolean active
) {
}

