package com.gymmate.notification.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Stripe charge is refunded (full or partial).
 * Informs the organisation owner about the refund.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeRefundedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        BigDecimal amount,
        String currency,
        String refundId,
        String paymentIntentId,
        String reason
) implements DomainEvent, TenantAwareEvent {

    public ChargeRefundedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public ChargeRefundedEvent(UUID organisationId, BigDecimal amount, String currency, String refundId, String paymentIntentId, String reason) {
        this(null, null, organisationId, amount, currency, refundId, paymentIntentId, reason);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @Override
    public UUID getOrganisationId() {
        return organisationId;
    }

    @Override
    public TenantIdentity getTenantIdentity() {
        return TenantIdentity.forOrganisation(organisationId);
    }

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
