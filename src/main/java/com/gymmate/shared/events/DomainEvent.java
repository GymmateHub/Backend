package com.gymmate.shared.events;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gymmate.shared.constants.NotificationPriority;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events in the system.
 * Domain events represent something that has happened in the business domain.
 */
public interface DomainEvent {

    /**
     * Get the unique identifier for this event.
     */
    UUID getEventId();

    /**
     * Get the timestamp when this event occurred.
     */
    LocalDateTime getOccurredAt();

    /**
     * Get the organisation ID associated with this event.
     */
    UUID getOrganisationId();

    /**
     * Get the type of event (for notification categorization).
     */
    @JsonIgnore
    String getEventType();

    /**
     * Get human-readable title for notification display.
     */
    @JsonIgnore
    String getNotificationTitle();

    /**
     * Get human-readable message for notification display.
     */
    @JsonIgnore
    String getNotificationMessage();

    /**
     * Get the priority level for this notification.
     */
    @JsonIgnore
    NotificationPriority getPriority();
}

