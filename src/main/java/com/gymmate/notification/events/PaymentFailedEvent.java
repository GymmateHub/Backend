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
 * Event published when a payment fails for an organisation's subscription.
 */
@Getter
@Builder
public class PaymentFailedEvent implements DomainEvent, TenantAwareEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID gymId;
    private final BigDecimal amount;
    private final String failureReason;
    private final LocalDateTime nextRetryDate;
    private final String invoiceId;

    /**
     * Set only for Stripe Connect (member-payment) failures — null for platform
     * subscription failures. Lets {@code membership.application.MembershipPaymentEventListener}
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

