package com.gymmate.notification.api.dto;

import com.gymmate.notification.domain.AudienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request DTO for creating a newsletter campaign.
 */
@Data
public class CreateCampaignRequest {

    @NotNull(message = "Gym ID is required")
    private UUID gymId;

    private UUID templateId;

    @Size(max = 100, message = "Campaign name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Audience type is required")
    private AudienceType audienceType;

    private String audienceFilter;

    private LocalDateTime scheduledAt;
}
