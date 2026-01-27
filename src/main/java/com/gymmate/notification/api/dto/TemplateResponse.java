package com.gymmate.notification.api.dto;

import com.gymmate.notification.domain.NewsletterTemplate;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for newsletter template.
 */
@Data
@Builder
public class TemplateResponse {

    private UUID id;
    private UUID gymId;
    private UUID organisationId;
    private String name;
    private String subject;
    private String body;
    private String templateType;
    private String placeholders;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TemplateResponse fromEntity(NewsletterTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .gymId(template.getGymId())
                .organisationId(template.getOrganisationId())
                .name(template.getName())
                .subject(template.getSubject())
                .body(template.getBody())
                .templateType(template.getTemplateType())
                .placeholders(template.getPlaceholders())
                .active(template.isActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
