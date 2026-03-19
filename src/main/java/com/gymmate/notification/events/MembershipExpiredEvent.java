package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a member's membership expires.
 */
@Getter
@Builder
public class MembershipExpiredEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID gymId;
    private final UUID memberId;
    private final UUID membershipId;
    private final LocalDate expiredOn;

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

