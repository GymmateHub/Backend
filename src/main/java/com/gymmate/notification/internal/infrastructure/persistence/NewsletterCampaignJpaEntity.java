package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.api.dto.AudienceType;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.notification.internal.domain.CampaignStatus;
import com.gymmate.notification.internal.domain.NewsletterCampaign;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link NewsletterCampaign} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "NewsletterCampaign")
@Table(name = "newsletter_campaigns")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(NewsletterCampaign.class)
public class NewsletterCampaignJpaEntity extends GymScopedJpaEntity {

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
    private Integer totalRecipients = 0;

    @Column(name = "delivered_count")
    private Integer deliveredCount = 0;

    @Column(name = "failed_count")
    private Integer failedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CampaignStatus status = CampaignStatus.DRAFT;

    @Column(name = "sent_by_user_id")
    private UUID sentByUserId;
}
