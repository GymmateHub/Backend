package com.gymmate.health.api.dto;

import com.gymmate.health.application.FitnessGoalService;
import com.gymmate.health.application.HealthAnalyticsService;

import java.util.List;

/**
 * Response DTO for comprehensive member health dashboard.
 */
public record MemberHealthDashboardResponse(
    List<WorkoutLogResponse> recentWorkouts,
    WorkoutStatisticsResponse workoutStats,
    BodyCompositionResponse bodyComposition,
    List<GoalWithProgressResponse> activeGoals,
    int workoutStreak,
    MetricTrendsResponse recentTrends,
    GoalStatisticsResponse goalStatistics
) {
    public static MemberHealthDashboardResponse from(
        HealthAnalyticsService.MemberHealthDashboard dashboard,
        List<WorkoutLogResponse> workoutResponses
    ) {
        return new MemberHealthDashboardResponse(
            workoutResponses,
            WorkoutStatisticsResponse.from(dashboard.workoutStats()),
            BodyCompositionResponse.from(dashboard.bodyComposition()),
            dashboard.activeGoals().stream()
                .map(GoalWithProgressResponse::from)
                .toList(),
            dashboard.workoutStreak(),
            MetricTrendsResponse.from(dashboard.recentTrends()),
            GoalStatisticsResponse.from(dashboard.goalStatistics())
        );
    }

    public record GoalWithProgressResponse(
        FitnessGoalResponse goal,
        java.math.BigDecimal progressPercentage,
        boolean isOverdue,
        long daysRemaining
    ) {
        public static GoalWithProgressResponse from(HealthAnalyticsService.GoalWithProgress gwp) {
            return new GoalWithProgressResponse(
                FitnessGoalResponse.from(gwp.goal()),
                gwp.progressPercentage(),
                gwp.isOverdue(),
                gwp.daysRemaining()
            );
        }
    }

    public record MetricTrendsResponse(
        MetricTrendResponse weightTrend,
        MetricTrendResponse bodyFatTrend,
        MetricTrendResponse muscleMassTrend
    ) {
        public static MetricTrendsResponse from(HealthAnalyticsService.MetricTrends trends) {
            return new MetricTrendsResponse(
                trends.weightTrend() != null ? MetricTrendResponse.from(trends.weightTrend()) : null,
                trends.bodyFatTrend() != null ? MetricTrendResponse.from(trends.bodyFatTrend()) : null,
                trends.muscleMassTrend() != null ? MetricTrendResponse.from(trends.muscleMassTrend()) : null
            );
        }
    }

    public record GoalStatisticsResponse(
        int totalGoals,
        int activeGoals,
        int achievedGoals,
        int abandonedGoals,
        int onHoldGoals,
        double successRate
    ) {
        public static GoalStatisticsResponse from(FitnessGoalService.GoalStatistics stats) {
            return new GoalStatisticsResponse(
                stats.totalGoals(),
                stats.activeGoals(),
                stats.achievedGoals(),
                stats.abandonedGoals(),
                stats.onHoldGoals(),
                stats.successRate()
            );
        }
    }
}
