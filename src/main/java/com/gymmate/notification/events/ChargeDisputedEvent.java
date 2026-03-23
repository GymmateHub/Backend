package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Stripe charge is disputed (chargeback).
 * Triggers critical notification to the organisation owner.
 */
@Getter
@Builder
public class ChargeDisputedEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final BigDecimal amount;
    private final String currency;
    private final String disputeId;
    private final String disputeReason;
    private final String paymentIntentId;

    @Override
    public String getEventType() {
        return "CHARGE_DISPUTED";
    }

    @Override
    public String getNotificationTitle() {
        return "🚨 Payment Dispute Received";
    }

    @Override
    public String getNotificationMessage() {
        return String.format(
                "A charge of %s %s has been disputed. Reason: %s. Dispute ID: %s. " +
                "Please respond promptly via your Stripe Dashboard to avoid automatic loss.",
                amount != null ? amount.toString() : "unknown",
                currency != null ? currency : "USD",
                disputeReason != null ? disputeReason : "Not specified",
                disputeId != null ? disputeId : "N/A");
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.CRITICAL;
    }
}

