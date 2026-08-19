package com.gymmate.notification.domain;

/**
 * Reasons why an email address is suppressed from receiving outbound emails.
 */
public enum SuppressionReason {
    PERMANENT_BOUNCE,
    TRANSIENT_BOUNCE,
    COMPLAINT,
    UNSUBSCRIBED,
    MANUAL
}
