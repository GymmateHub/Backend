package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for a member booking a class for themselves.
 * gymId and memberId are derived from the authenticated user.
 */
public record SelfBookingRequest(
    @NotNull(message = "Schedule ID is required") UUID scheduleId,
    String notes) {
}
