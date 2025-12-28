package com.gymmate.payment.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity for tracking processed Stripe webhook events to ensure idempotency.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "stripe_webhook_events")
public class StripeWebhookEvent extends BaseAuditEntity {

    @Column(name = "stripe_event_id", unique = true, nullable = false)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column
    @Builder.Default
    private Boolean processed = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public void markProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.processed = false;
        this.errorMessage = errorMessage;
    }
}

