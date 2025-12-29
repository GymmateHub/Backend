package com.gymmate.gym.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for gym analytics dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymAnalyticsResponse {

    // Gym identification (if querying specific gym)
    private UUID gymId;
    private String gymName;

    // Total gyms owned by the user
    private int totalGyms;

    // Total capacity across all gyms owned by the user
    private int totalCapacity;

    // Active locations (gyms with ACTIVE status)
    private int activeLocations;

    // Average utilization percentage across all gyms
    private double avgUtilization;

    // Total revenue this month
    private BigDecimal totalRevenue;

    // Additional metrics
    private int totalMembers;
    private int totalActiveMembers;
    private int totalStaff;
    private int totalTrainers;

    // Gym-specific metrics (when querying a single gym)
    private Integer currentMembers;
    private Integer maxMembers;
    private Double utilizationPercentage;
}

