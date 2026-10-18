package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.notification.internal.domain.NotificationChannel;
import com.gymmate.notification.internal.domain.RecipientStatus;
import com.gymmate.notification.internal.domain.CampaignRecipient;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link CampaignRecipient} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "CampaignRecipient")
@Table(name = "campaign_recipients")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(CampaignRecipient.class)
public class CampaignRecipientJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
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
    private boolean fallbackUsed = false;
}
