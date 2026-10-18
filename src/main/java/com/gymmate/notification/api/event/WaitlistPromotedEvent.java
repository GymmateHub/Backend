package com.gymmate.notification.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a member is promoted from waitlist to confirmed booking.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WaitlistPromotedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID gymId,
        UUID memberId,
        UUID bookingId,
        UUID scheduleId
) implements DomainEvent, TenantAwareEvent {

    public WaitlistPromotedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public WaitlistPromotedEvent(UUID organisationId, UUID gymId, UUID memberId, UUID bookingId, UUID scheduleId) {
        this(null, null, organisationId, gymId, memberId, bookingId, scheduleId);
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
        return "WAITLIST_PROMOTED";
    }

    @Override
    public String getNotificationTitle() {
        return "🎉 You're In! Waitlist Promotion";
    }

    @Override
    public String getNotificationMessage() {
        return "Great news! A spot has opened up and your booking has been confirmed. You've been promoted from the waitlist.";
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.HIGH;
    }
}
