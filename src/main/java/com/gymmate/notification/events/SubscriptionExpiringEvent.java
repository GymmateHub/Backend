package com.gymmate.notification.events;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a subscription is expiring soon (e.g., trial ending).
 */
@Getter
@Builder
public class SubscriptionExpiringEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID subscriptionId;
    private final String tierName;
    private final BigDecimal price;
    private final LocalDateTime expiresAt;
    private final int daysUntilExpiry;

    @Override
    public String getEventType() {
        return "SUBSCRIPTION_EXPIRING";
    }

    @Override
    public String getNotificationTitle() {
        return "⏰ Subscription Expiring Soon";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("Your %s subscription expires in %d days (%s)",
                tierName,
                daysUntilExpiry,
                expiresAt.toLocalDate().toString());
    }

    @Override
    public NotificationPriority getPriority() {
        return daysUntilExpiry <= 3 ? NotificationPriority.HIGH : NotificationPriority.MEDIUM;
    }
}

