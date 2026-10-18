package com.gymmate.organisation.internal.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for gym analytics dashboard.
 */
public record GymAnalyticsResponse(
        // Gym identification (if querying specific gym)
        UUID gymId,
        String gymName,
        // Total gyms owned by the user
        int totalGyms,
        // Total capacity across all gyms owned by the user
        int totalCapacity,
        // Active locations (gyms with ACTIVE status)
        int activeLocations,
        // Average utilization percentage across all gyms
        double avgUtilization,
        // Total revenue this month
        BigDecimal totalRevenue,
        // Additional metrics
        int totalMembers,
        int totalActiveMembers,
        int totalStaff,
        int totalTrainers,
        // Gym-specific metrics (when querying a single gym)
        Integer currentMembers,
        Integer maxMembers,
        Double utilizationPercentage
) {
}
