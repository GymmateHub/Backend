package com.gymmate.notification.events;

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
public class PaymentFailedEvent implements DomainEvent {

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

