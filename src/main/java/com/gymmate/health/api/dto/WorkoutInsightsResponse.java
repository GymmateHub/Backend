package com.gymmate.health.api.dto;

import com.gymmate.health.application.HealthAnalyticsService;
import com.gymmate.health.domain.Enums.WorkoutIntensity;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for workout insights and analytics.
 */
public record WorkoutInsightsResponse(
    int totalWorkouts,
    int totalMinutes,
    int totalCalories,
    double avgWorkoutsPerWeek,
    Map<String, Long> timeOfDayDistribution,
    Map<WorkoutIntensity, Long> intensityDistribution,
    List<String> recommendations
) {
    public static WorkoutInsightsResponse from(HealthAnalyticsService.WorkoutInsights insights) {
        return new WorkoutInsightsResponse(
            insights.totalWorkouts(),
            insights.totalMinutes(),
            insights.totalCalories(),
            insights.avgWorkoutsPerWeek(),
            insights.timeOfDayDistribution(),
            insights.intensityDistribution(),
            insights.recommendations()
        );
    }
}

