package com.gymmate.notification.internal.application.dto;

import com.gymmate.notification.internal.domain.NewsletterTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for newsletter template.
 */
public record TemplateResponse(
        UUID id,
        UUID gymId,
        UUID organisationId,
        String name,
        String subject,
        String body,
        String templateType,
        String placeholders,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TemplateResponse fromEntity(NewsletterTemplate template) {
        return new TemplateResponse(
                template.getId(),
                template.getGymId(),
                template.getOrganisationId(),
                template.getName(),
                template.getSubject(),
                template.getBody(),
                template.getTemplateType(),
                template.getPlaceholders(),
                template.isActive(),
                template.getCreatedAt(),
                template.getUpdatedAt());
    }
}
