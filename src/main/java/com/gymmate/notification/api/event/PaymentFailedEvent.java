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
 * Event published when a payment fails for an organisation's subscription.
 *
 * @param membershipId Set only for Stripe Connect (member-payment) failures — null for platform
 *     subscription failures. Lets {@code membership.application.MembershipPaymentEventListener}
 *     react without {@code payment} depending on {@code membership} directly (see that
 *     listener's Javadoc for why: it used to be a direct repository write from
 *     {@code StripeWebhookService}, which created a module dependency cycle).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentFailedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID gymId,
        BigDecimal amount,
        String failureReason,
        LocalDateTime nextRetryDate,
        String invoiceId,
        UUID membershipId,
        String currency
) implements DomainEvent, TenantAwareEvent {

    public PaymentFailedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public PaymentFailedEvent(UUID organisationId, UUID gymId, BigDecimal amount, String failureReason, LocalDateTime nextRetryDate, String invoiceId, UUID membershipId, String currency) {
        this(null, null, organisationId, gymId, amount, failureReason, nextRetryDate, invoiceId, membershipId, currency);
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
        return "PAYMENT_FAILED";
    }

    @Override
    public String getNotificationTitle() {
        return "⚠️ Payment Failed";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("Payment of $%s failed: %s. Next retry: %s",
                amount.toString(),
                failureReason != null ? failureReason : "Unknown reason",
                nextRetryDate != null ? nextRetryDate.toString() : "Not scheduled");
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.CRITICAL;
    }
}
