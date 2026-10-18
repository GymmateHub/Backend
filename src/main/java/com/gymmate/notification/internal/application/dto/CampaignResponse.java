package com.gymmate.notification.internal.application.dto;

import com.gymmate.notification.api.dto.AudienceType;
import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for newsletter campaign.
 */
public record CampaignResponse(
        UUID id,
        UUID gymId,
        UUID organisationId,
        UUID templateId,
        String name,
        String subject,
        String body,
        AudienceType audienceType,
        String audienceFilter,
        LocalDateTime scheduledAt,
        LocalDateTime sentAt,
        Integer totalRecipients,
        Integer deliveredCount,
        Integer failedCount,
        CampaignStatus status,
        UUID sentByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CampaignResponse fromEntity(NewsletterCampaign campaign) {
        return new CampaignResponse(
                campaign.getId(),
                campaign.getGymId(),
                campaign.getOrganisationId(),
                campaign.getTemplateId(),
                campaign.getName(),
                campaign.getSubject(),
                campaign.getBody(),
                campaign.getAudienceType(),
                campaign.getAudienceFilter(),
                campaign.getScheduledAt(),
                campaign.getSentAt(),
                campaign.getTotalRecipients(),
                campaign.getDeliveredCount(),
                campaign.getFailedCount(),
                campaign.getStatus(),
                campaign.getSentByUserId(),
                campaign.getCreatedAt(),
                campaign.getUpdatedAt());
    }
}
