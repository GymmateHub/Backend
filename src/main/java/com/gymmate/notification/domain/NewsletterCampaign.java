package com.gymmate.notification.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a newsletter campaign.
 * A campaign is a bulk email sent to a targeted audience.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "newsletter_campaigns")
public class NewsletterCampaign extends GymScopedEntity {

    @Column(name = "template_id")
    private UUID templateId;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false, length = 30)
    private AudienceType audienceType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audience_filter", columnDefinition = "jsonb")
    private String audienceFilter;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "total_recipients")
    @Builder.Default
    private Integer totalRecipients = 0;

    @Column(name = "delivered_count")
    @Builder.Default
    private Integer deliveredCount = 0;

    @Column(name = "failed_count")
    @Builder.Default
    private Integer failedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "sent_by_user_id")
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
