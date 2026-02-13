package com.gymmate.notification.events;

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
public class PaymentSuccessEvent implements DomainEvent {

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

