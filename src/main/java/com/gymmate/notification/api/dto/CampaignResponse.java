package com.gymmate.notification.api.dto;

import com.gymmate.notification.domain.AudienceType;
import com.gymmate.notification.domain.CampaignStatus;
import com.gymmate.notification.domain.NewsletterCampaign;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for newsletter campaign.
 */
@Data
@Builder
public class CampaignResponse {

    private UUID id;
    private UUID gymId;
    private UUID organisationId;
    private UUID templateId;
    private String name;
    private String subject;
    private String body;
    private AudienceType audienceType;
    private String audienceFilter;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Integer totalRecipients;
    private Integer deliveredCount;
    private Integer failedCount;
    private CampaignStatus status;
    private UUID sentByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CampaignResponse fromEntity(NewsletterCampaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .gymId(campaign.getGymId())
                .organisationId(campaign.getOrganisationId())
                .templateId(campaign.getTemplateId())
                .name(campaign.getName())
                .subject(campaign.getSubject())
                .body(campaign.getBody())
                .audienceType(campaign.getAudienceType())
                .audienceFilter(campaign.getAudienceFilter())
                .scheduledAt(campaign.getScheduledAt())
                .sentAt(campaign.getSentAt())
                .totalRecipients(campaign.getTotalRecipients())
                .deliveredCount(campaign.getDeliveredCount())
                .failedCount(campaign.getFailedCount())
                .status(campaign.getStatus())
                .sentByUserId(campaign.getSentByUserId())
                .createdAt(campaign.getCreatedAt())
                .updatedAt(campaign.getUpdatedAt())
                .build();
    }
}
