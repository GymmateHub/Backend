package com.gymmate.notification.internal.domain;

import com.gymmate.notification.api.dto.AudienceType;
import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a newsletter campaign.
 * A campaign is a bulk email sent to a targeted audience.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class NewsletterCampaign extends GymScopedEntity {

    private UUID templateId;

    private String name;

    private String subject;

    private String body;

    private 
    AudienceType audienceType;

    private 
    String audienceFilter;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Builder.Default
    private 
    Integer totalRecipients = 0;

    @Builder.Default
    private 
    Integer deliveredCount = 0;

    @Builder.Default
    private 
    Integer failedCount = 0;

    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    private UUID sentByUserId;

    /**
     * Schedule the campaign for future delivery.
     */
    public void schedule(LocalDateTime scheduledAt) {
        if (this.status != CampaignStatus.DRAFT) {
            throw new DomainException("CAMPAIGN_NOT_DRAFT", "Only draft campaigns can be scheduled");
        }
        if (scheduledAt.isBefore(LocalDateTime.now())) {
            throw new DomainException("INVALID_SCHEDULE_TIME", "Scheduled time must be in the future");
        }
        this.scheduledAt = scheduledAt;
        this.status = CampaignStatus.SCHEDULED;
    }

    /**
     * Mark campaign as sending (in progress).
     */
    public void startSending() {
        if (this.status != CampaignStatus.DRAFT && this.status != CampaignStatus.SCHEDULED) {
            throw new DomainException("CAMPAIGN_CANNOT_SEND", "Campaign is not in a valid state to send");
        }
        this.status = CampaignStatus.SENDING;
    }

    /**
     * Mark campaign as sent.
     */
    public void completeSending(int totalRecipients, int deliveredCount, int failedCount) {
        this.sentAt = LocalDateTime.now();
        this.totalRecipients = totalRecipients;
        this.deliveredCount = deliveredCount;
        this.failedCount = failedCount;
        this.status = failedCount == totalRecipients ? CampaignStatus.FAILED : CampaignStatus.SENT;
    }

    /**
     * Cancel a scheduled campaign.
     */
    public void cancel() {
        if (this.status == CampaignStatus.SENT || this.status == CampaignStatus.SENDING) {
            throw new DomainException("CAMPAIGN_CANNOT_CANCEL",
                    "Cannot cancel a campaign that is already sent or sending");
        }
        this.status = CampaignStatus.CANCELLED;
    }

    /**
     * Update campaign content before sending.
     */
    public void updateContent(String name, String subject, String body) {
        if (this.status != CampaignStatus.DRAFT) {
            throw new DomainException("CAMPAIGN_NOT_DRAFT", "Only draft campaigns can be modified");
        }
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    /**
     * Check if campaign can be sent.
     */
    public boolean canSend() {
        return this.status == CampaignStatus.DRAFT || this.status == CampaignStatus.SCHEDULED;
    }
}
