package com.gymmate.notification.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity tracking individual campaign recipients and delivery status.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CampaignRecipient extends BaseAuditEntity {

    private UUID campaignId;

    private UUID memberId;

    private String email;

    @Builder.Default
    private RecipientStatus status = RecipientStatus.PENDING;

    private LocalDateTime sentAt;

    private LocalDateTime deliveredAt;

    private String errorMessage;

    private NotificationChannel channelUsed;

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
