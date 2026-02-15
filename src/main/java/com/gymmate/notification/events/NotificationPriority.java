package com.gymmate.notification.events;

/**
 * Priority levels for notifications.
 * Determines urgency and display prominence in the admin dashboard.
 */
public enum NotificationPriority {
    /**
     * Critical issues requiring immediate attention (e.g., payment failures, system errors).
     */
    CRITICAL,

    /**
     * Important issues that need attention soon (e.g., trial expiring, chargebacks).
     */
    HIGH,

    /**
     * Normal priority notifications (e.g., subscription renewals, member joins).
     */
    MEDIUM,

    /**
     * Low priority informational notifications (e.g., reports, analytics).
     */
    LOW
}

