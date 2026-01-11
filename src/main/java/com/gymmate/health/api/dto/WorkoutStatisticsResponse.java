package com.gymmate.health.api.dto;

import com.gymmate.health.application.WorkoutTrackingService;
import com.gymmate.health.domain.Enums.WorkoutIntensity;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for workout statistics.
 */
public record WorkoutStatisticsResponse(
    int totalWorkouts,
    int totalDurationMinutes,
    int totalCaloriesBurned,
    int averageDurationMinutes,
    WorkoutIntensity averageIntensity,
    Map<UUID, Long> exerciseFrequency
) {
    public static WorkoutStatisticsResponse from(WorkoutTrackingService.WorkoutStatistics stats) {
        return new WorkoutStatisticsResponse(
            stats.totalWorkouts(),
            stats.totalDurationMinutes(),
            stats.totalCaloriesBurned(),
            stats.averageDurationMinutes(),
            stats.averageIntensity(),
            stats.exerciseFrequency()
        );
    }
}
