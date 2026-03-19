package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Stripe charge is refunded (full or partial).
 * Informs the organisation owner about the refund.
 */
@Getter
@Builder
public class ChargeRefundedEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final BigDecimal amount;
    private final String currency;
    private final String refundId;
    private final String paymentIntentId;
    private final String reason;

    @Override
    public String getEventType() {
        return "CHARGE_REFUNDED";
    }

    @Override
    public String getNotificationTitle() {
        return "💰 Charge Refunded";
    }

    @Override
    public String getNotificationMessage() {
        return String.format(
                "A refund of %s %s has been processed. Reason: %s. Refund ID: %s",
                amount != null ? amount.toString() : "unknown",
                currency != null ? currency : "USD",
                reason != null ? reason : "Not specified",
                refundId != null ? refundId : "N/A");
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.HIGH;
    }
}

