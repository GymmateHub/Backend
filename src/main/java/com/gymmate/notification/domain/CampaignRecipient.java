package com.gymmate.notification.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity tracking individual campaign recipients and delivery status.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "campaign_recipients")
public class CampaignRecipient extends BaseAuditEntity {

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RecipientStatus status = RecipientStatus.PENDING;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_used", length = 20)
    private NotificationChannel channelUsed;

    @Column(name = "fallback_used")
    @Builder.Default
    private boolean fallbackUsed = false;

    /**
     * Mark as sent successfully via a specific channel.
     */
    public void markSent(NotificationChannel channel, boolean usedFallback) {
        this.status = RecipientStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.channelUsed = channel;
        this.fallbackUsed = usedFallback;
    }

    /**
     * Mark as sent successfully (legacy method, defaults to EMAIL).
     */
    public void markSent() {
        markSent(NotificationChannel.EMAIL, false);
    }

    /**
     * Mark as delivered (confirmed).
     */
    public void markDelivered() {
        this.status = RecipientStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Mark as failed with error message.
     */
    public void markFailed(String errorMessage) {
        this.status = RecipientStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
