package com.gymmate.user.api.dto;

import com.gymmate.health.application.HealthMetricService.BodyCompositionSnapshot;
import com.gymmate.health.application.WorkoutTrackingService.WorkoutStatistics;

/**
 * Progress summary for the member mobile app:
 * last-30-day workout statistics, current streak, and latest body composition.
 */
public record MemberProgressResponse(
    WorkoutStatistics workoutStatistics,
    int workoutStreak,
    BodyCompositionSnapshot bodyComposition) {
}
