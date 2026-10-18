package com.gymmate.notification.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a member's membership expires.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MembershipExpiredEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID organisationId,
        UUID gymId,
        UUID memberId,
        UUID membershipId,
        LocalDate expiredOn
) implements DomainEvent, TenantAwareEvent {

    public MembershipExpiredEvent {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = LocalDateTime.now();
    }

    /** A new event with a fresh id, occurring now. */
    public MembershipExpiredEvent(UUID organisationId, UUID gymId, UUID memberId, UUID membershipId, LocalDate expiredOn) {
        this(null, null, organisationId, gymId, memberId, membershipId, expiredOn);
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
        return "MEMBERSHIP_EXPIRED";
    }

    @Override
    public String getNotificationTitle() {
        return "⏰ Membership Expired";
    }

    @Override
    public String getNotificationMessage() {
        return String.format("A membership expired on %s. Please renew to continue accessing gym services.", expiredOn);
    }

    @Override
    public NotificationPriority getPriority() {
        return NotificationPriority.HIGH;
    }
}
