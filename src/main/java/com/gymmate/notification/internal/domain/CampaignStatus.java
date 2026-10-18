package com.gymmate.notification.internal.domain;

/**
 * Enum representing the status of a newsletter campaign.
 */
public enum CampaignStatus {
    DRAFT,
    SCHEDULED,
    SENDING,
    SENT,
    FAILED,
    CANCELLED
}
