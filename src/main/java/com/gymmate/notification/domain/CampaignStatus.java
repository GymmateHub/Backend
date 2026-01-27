package com.gymmate.notification.domain;

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
