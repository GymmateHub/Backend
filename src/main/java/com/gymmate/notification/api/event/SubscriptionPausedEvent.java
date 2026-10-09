package com.gymmate.notification.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a Stripe subscription is paused.
 * Informs the organisation owner that their subscription has been paused.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionPausedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID subscriptionId,
        String tierName,
        LocalDateTime pausedAt
) implements DomainEvent, TenantAwareEvent {

    public SubscriptionPausedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public SubscriptionPausedEvent(UUID organisationId, UUID subscriptionId, String tierName, LocalDateTime pausedAt) {
        this(null, null, organisationId, subscriptionId, tierName, pausedAt);
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
