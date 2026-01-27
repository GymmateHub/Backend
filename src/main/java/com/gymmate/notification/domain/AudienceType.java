package com.gymmate.notification.domain;

/**
 * Enum representing the target audience type for a newsletter campaign.
 */
public enum AudienceType {
    /**
     * All active members of the gym.
     */
    ALL_MEMBERS,

    /**
     * Members subscribed to specific classes.
     */
    CLASS_SUBSCRIBERS,

    /**
     * Members with active bookings.
     */
    BOOKING_PARTICIPANTS,

    /**
     * Members on a specific membership plan.
     */
    MEMBERSHIP_PLAN,

    /**
     * Custom member selection.
     */
    CUSTOM
}
