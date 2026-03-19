package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Stripe subscription is paused.
 * Informs the organisation owner that their subscription has been paused.
 */
@Getter
@Builder
public class SubscriptionPausedEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID subscriptionId;
    private final String tierName;
    private final LocalDateTime pausedAt;

    @Override
    public String getEventType() {
        return "SUBSCRIPTION_PAUSED";
    }

    @Override
    public String getNotificationTitle() {
        return "⏸️ Subscription Paused";
    }

    @Override
    public String getNotificationMessage() {
        return String.format(
                "Your %s subscription has been paused. Some features may be limited. " +
                "To resume, update your billing in the dashboard.",
                tierName != null ? tierName : "subscription");
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.HIGH;
    }
}

