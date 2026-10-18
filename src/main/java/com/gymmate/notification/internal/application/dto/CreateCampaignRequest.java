package com.gymmate.notification.internal.application.dto;

import com.gymmate.notification.api.dto.AudienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a newsletter campaign.
 */
public record CreateCampaignRequest(
        @NotNull(message = "Gym ID is required")
        UUID gymId,
        UUID templateId,
        @Size(max = 100, message = "Campaign name must be less than 100 characters")
        String name,
        @NotBlank(message = "Subject is required")
        @Size(max = 255, message = "Subject must be less than 255 characters")
        String subject,
        @NotBlank(message = "Body is required")
        String body,
        @NotNull(message = "Audience type is required")
        AudienceType audienceType,
        String audienceFilter,
        LocalDateTime scheduledAt
) {
}
