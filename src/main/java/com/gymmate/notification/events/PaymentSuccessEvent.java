package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a payment is successfully processed.
 */
@Getter
@Builder
public class PaymentSuccessEvent implements DomainEvent, TenantAwareEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID gymId;
    private final BigDecimal amount;
    private final String invoiceNumber;
    private final String invoiceUrl;
    private final LocalDateTime periodEnd;

    /**
     * Set only for Stripe Connect (member-payment) successes — null for platform
     * subscription payments. Lets {@code membership.application.MembershipPaymentEventListener}
     * react without {@code payment} depending on {@code membership} directly (see that
     * listener's Javadoc for why: it used to be a direct repository write from
     * {@code StripeWebhookService}, which created a module dependency cycle).
     */
    private final UUID membershipId;
    private final String currency;

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

