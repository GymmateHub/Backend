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
 * Event published when a subscription is expiring soon (e.g., trial ending).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SubscriptionExpiringEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID subscriptionId,
        String tierName,
        BigDecimal price,
        LocalDateTime expiresAt,
        int daysUntilExpiry
) implements DomainEvent, TenantAwareEvent {

    public SubscriptionExpiringEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public SubscriptionExpiringEvent(UUID organisationId, UUID subscriptionId, String tierName, BigDecimal price, LocalDateTime expiresAt, int daysUntilExpiry) {
        this(null, null, organisationId, subscriptionId, tierName, price, expiresAt, daysUntilExpiry);
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
