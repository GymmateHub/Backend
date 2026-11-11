package com.gymmate.configuration.internal.application.dto;

import com.gymmate.configuration.internal.domain.MessageTemplateChannel;
import com.gymmate.configuration.internal.domain.MessageTemplateConfig;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a resolved or stored message template configuration.
 */
public record MessageTemplateConfigResponse(
        UUID id,
        UUID organisationId,
        String templateKey,
        MessageTemplateChannel channel,
        String name,
        String description,
        String subjectOrTitle,
        String bodyTemplate,
        String classpathTemplate,
        String availableVariables,
        boolean bodyCustomised,
        boolean active,
        boolean organisationOverride,
        LocalDateTime updatedAt
) {

    public static MessageTemplateConfigResponse from(MessageTemplateConfig config, boolean organisationOverride) {
        return new MessageTemplateConfigResponse(
                config.getId(),
                config.getOrganisationId(),
                config.getTemplateKey(),
                config.getChannel(),
                config.getName(),
                config.getDescription(),
                config.getSubjectOrTitle(),
                config.getBodyTemplate(),
                config.getClasspathTemplate(),
                config.getAvailableVariables(),
                config.isBodyCustomised(),
                config.isActive(),
                organisationOverride,
                config.getUpdatedAt()
        );
    }
}

