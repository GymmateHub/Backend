package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import com.gymmate.billing.internal.domain.StripeWebhookEvent;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link StripeWebhookEvent} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "StripeWebhookEvent")
@Table(name = "stripe_webhook_events")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(StripeWebhookEvent.class)
public class StripeWebhookEventJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "stripe_event_id", unique = true, nullable = false)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column
    private Boolean processed = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
