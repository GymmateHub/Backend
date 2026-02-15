package com.gymmate.notification.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a new member joins a gym.
 */
@Getter
@Builder
public class MemberJoinedEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID gymId;
    private final UUID memberId;
    private final String memberName;
    private final String memberEmail;
    private final String membershipPlan;

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

