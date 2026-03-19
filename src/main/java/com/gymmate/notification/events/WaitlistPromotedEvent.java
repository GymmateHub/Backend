package com.gymmate.notification.events;

import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a member is promoted from waitlist to confirmed booking.
 */
@Getter
@Builder
public class WaitlistPromotedEvent implements DomainEvent {

    @Builder.Default
    private final UUID eventId = UUID.randomUUID();

    @Builder.Default
    private final LocalDateTime occurredAt = LocalDateTime.now();

    private final UUID organisationId;
    private final UUID gymId;
    private final UUID memberId;
    private final UUID bookingId;
    private final UUID scheduleId;

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

