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
 * Event published when a Stripe charge is disputed (chargeback).
 * Triggers critical notification to the organisation owner.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChargeDisputedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        BigDecimal amount,
        String currency,
        String disputeId,
        String disputeReason,
        String paymentIntentId
) implements DomainEvent, TenantAwareEvent {

    public ChargeDisputedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public ChargeDisputedEvent(UUID organisationId, BigDecimal amount, String currency, String disputeId, String disputeReason, String paymentIntentId) {
        this(null, null, organisationId, amount, currency, disputeId, disputeReason, paymentIntentId);
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
