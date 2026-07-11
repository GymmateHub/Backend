package com.gymmate.user.api.dto;

import java.time.LocalDateTime;

/**
 * A notification item for the member mobile app.
 * Computed from the member's own data (bookings, membership, waiver)
 * rather than stored — the notifications table is staff/owner-facing.
 */
public record MemberNotificationItem(
    String type, // UPCOMING_CLASS, MEMBERSHIP_EXPIRING, MEMBERSHIP_FROZEN, WAIVER_PENDING, WAITLISTED
    String title,
    String message,
    LocalDateTime date) {
}
