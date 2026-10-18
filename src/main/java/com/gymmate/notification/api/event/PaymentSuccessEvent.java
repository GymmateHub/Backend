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
 * Event published when a payment is successfully processed.
 *
 * @param membershipId Set only for Stripe Connect (member-payment) successes — null for platform
 *     subscription payments. Lets {@code membership.application.MembershipPaymentEventListener}
 *     react without {@code payment} depending on {@code membership} directly (see that
 *     listener's Javadoc for why: it used to be a direct repository write from
 *     {@code StripeWebhookService}, which created a module dependency cycle).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSuccessEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID gymId,
        BigDecimal amount,
        String invoiceNumber,
        String invoiceUrl,
        LocalDateTime periodEnd,
        UUID membershipId,
        String currency
) implements DomainEvent, TenantAwareEvent {

    public PaymentSuccessEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public PaymentSuccessEvent(UUID organisationId, UUID gymId, BigDecimal amount, String invoiceNumber, String invoiceUrl, LocalDateTime periodEnd, UUID membershipId, String currency) {
        this(null, null, organisationId, gymId, amount, invoiceNumber, invoiceUrl, periodEnd, membershipId, currency);
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
        return TenantIdentity.of(organisationId, gymId);
    }

    @Override
    public String getEventType() {
        return "PAYMENT_SUCCESS";
    }

    @Override
    public String getNotificationTitle() {
        return "✅ Payment Received";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("Payment of $%s received successfully. Invoice: %s",
                amount.toString(),
                invoiceNumber);
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.LOW;
    }
}
