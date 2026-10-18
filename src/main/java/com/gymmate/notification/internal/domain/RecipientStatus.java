package com.gymmate.notification.internal.domain;

/**
 * Enum representing the delivery status of a campaign recipient.
 */
public enum RecipientStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    BOUNCED
}
