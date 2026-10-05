package com.gymmate.billing.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for tracking processed Stripe webhook events to ensure idempotency.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class StripeWebhookEvent extends BaseAuditEntity {

    private String stripeEventId;

    private String eventType;

    @Builder.Default
    private 
    Boolean processed = false;

    private 
    String payload;

    private String errorMessage;

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
