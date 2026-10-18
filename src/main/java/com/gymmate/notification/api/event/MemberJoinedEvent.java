package com.gymmate.notification.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new member joins a gym.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberJoinedEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID gymId,
        UUID memberId,
        String memberName,
        String memberEmail,
        String membershipPlan
) implements DomainEvent, TenantAwareEvent {

    public MemberJoinedEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public MemberJoinedEvent(UUID organisationId, UUID gymId, UUID memberId, String memberName, String memberEmail, String membershipPlan) {
        this(null, null, organisationId, gymId, memberId, memberName, memberEmail, membershipPlan);
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
        return "MEMBER_JOINED";
    }

    @Override
    public String getNotificationTitle() {
        return "👋 New Member Joined";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("%s joined on %s plan",
                memberName,
                membershipPlan != null ? membershipPlan : "Standard");
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.LOW;
    }
}
