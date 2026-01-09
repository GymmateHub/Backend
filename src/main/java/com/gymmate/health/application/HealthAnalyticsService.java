package com.gymmate.health.application;

import com.gymmate.health.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for health analytics and insights generation.
 * Aggregates data from workouts, metrics, and goals to provide comprehensive health dashboards.
 * Implements FR-015: Health Insights & Goals (Analytics).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthAnalyticsService {

    private final WorkoutLogRepository workoutLogRepository;
    private final HealthMetricRepository healthMetricRepository;
    private final FitnessGoalRepository fitnessGoalRepository;
    private final WorkoutTrackingService workoutTrackingService;
    private final HealthMetricService healthMetricService;
    private final FitnessGoalService fitnessGoalService;

    /**
     * Generate comprehensive health dashboard for a member.
     */
    @Transactional(readOnly = true)
    public MemberHealthDashboard generateMemberDashboard(UUID memberId) {
        log.debug("Generating health dashboard for member: {}", memberId);

        // Recent workouts
        LocalDate oneWeekAgo = LocalDate.now().minusWeeks(1);
        List<WorkoutLog> recentWorkouts = workoutLogRepository.findByMemberIdAndDateRange(
            memberId,
            oneWeekAgo.atStartOfDay(),
            LocalDateTime.now()
        );

        // Workout statistics (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        WorkoutTrackingService.WorkoutStatistics workoutStats =
            workoutTrackingService.calculateStatistics(memberId, thirtyDaysAgo, LocalDate.now());

        // Latest body composition
        HealthMetricService.BodyCompositionSnapshot bodyComposition =
            healthMetricService.getLatestBodyComposition(memberId);

        // Active goals with progress
        List<FitnessGoal> activeGoals = fitnessGoalRepository.findActiveByMemberId(memberId);
        List<GoalWithProgress> goalsWithProgress = activeGoals.stream()
            .map(goal -> new GoalWithProgress(
                goal,
                goal.calculateProgress(),
                goal.isOverdue(),
                goal.getDaysRemaining()
            ))
            .collect(Collectors.toList());

        // Workout streak
        int workoutStreak = workoutTrackingService.calculateWorkoutStreak(memberId);

        // Recent metric trends
        MetricTrends recentTrends = analyzeRecentTrends(memberId, 30);

        // Goal statistics
        FitnessGoalService.GoalStatistics goalStats = fitnessGoalService.getMemberGoalStatistics(memberId);

        return new MemberHealthDashboard(
            recentWorkouts,
            workoutStats,
            bodyComposition,
            goalsWithProgress,
            workoutStreak,
            recentTrends,
            goalStats
        );
    }

    /**
     * Generate workout insights for a member.
     */
    @Transactional(readOnly = true)
    public WorkoutInsights generateWorkoutInsights(UUID memberId, int days) {
        log.debug("Generating workout insights for member {} over {} days", memberId, days);

        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();

        // Get workouts in period
        List<WorkoutLog> workouts = workoutLogRepository.findByMemberIdAndDateRange(
            memberId,
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );

        if (workouts.isEmpty()) {
            return new WorkoutInsights(
                0, 0, 0, 0.0,
                Map.of(), Map.of(), List.of()
            );
        }

        // Calculate statistics
        int totalWorkouts = workouts.size();
        int totalMinutes = workouts.stream()
            .filter(w -> w.getDurationMinutes() != null)
            .mapToInt(WorkoutLog::getDurationMinutes)
            .sum();

        int totalCalories = workouts.stream()
            .filter(w -> w.getTotalCaloriesBurned() != null)
            .mapToInt(WorkoutLog::getTotalCaloriesBurned)
            .sum();

        double avgWorkoutsPerWeek = (totalWorkouts * 7.0) / days;

        // Most productive time of day (morning, afternoon, evening)
        Map<String, Long> timeOfDayDistribution = workouts.stream()
            .collect(Collectors.groupingBy(
                w -> getTimeOfDay(w.getWorkoutDate()),
                Collectors.counting()
            ));

        // Intensity distribution
        Map<WorkoutIntensity, Long> intensityDistribution = workouts.stream()
            .filter(w -> w.getIntensityLevel() != null)
            .collect(Collectors.groupingBy(
                WorkoutLog::getIntensityLevel,
                Collectors.counting()
            ));

        // Recommendations
        List<String> recommendations = generateWorkoutRecommendations(
            totalWorkouts, avgWorkoutsPerWeek, intensityDistribution, days
        );

        return new WorkoutInsights(
            totalWorkouts,
            totalMinutes,
            totalCalories,
            avgWorkoutsPerWeek,
            timeOfDayDistribution,
            intensityDistribution,
            recommendations
        );
    }

    /**
     * Generate health insights and recommendations.
     */
    @Transactional(readOnly = true)
    public HealthInsights generateHealthInsights(UUID memberId) {
        log.debug("Generating health insights for member: {}", memberId);

        // Analyze key metric trends over 30 days
        MetricTrends trends = analyzeRecentTrends(memberId, 30);

        // Get active goals progress
        List<FitnessGoal> activeGoals = fitnessGoalRepository.findActiveByMemberId(memberId);

        // Generate recommendations
        List<String> recommendations = generateHealthRecommendations(trends, activeGoals);

        // Generate warnings
        List<String> warnings = generateHealthWarnings(trends, activeGoals);

        return new HealthInsights(
            trends,
            recommendations,
            warnings,
            LocalDateTime.now()
        );
    }

    /**
     * Analyze recent metric trends.
     */
    @Transactional(readOnly = true)
    public MetricTrends analyzeRecentTrends(UUID memberId, int days) {
        log.debug("Analyzing metric trends for member {} over {} days", memberId, days);

        HealthMetricService.MetricTrend weightTrend = null;
        HealthMetricService.MetricTrend bodyFatTrend = null;
        HealthMetricService.MetricTrend muscleMassTrend = null;

        try {
            weightTrend = healthMetricService.analyzeMetricTrend(memberId, MetricType.WEIGHT, days);
        } catch (Exception e) {
            log.debug("Could not analyze weight trend: {}", e.getMessage());
        }

        try {
            bodyFatTrend = healthMetricService.analyzeMetricTrend(memberId, MetricType.BODY_FAT_PERCENTAGE, days);
        } catch (Exception e) {
            log.debug("Could not analyze body fat trend: {}", e.getMessage());
        }

        try {
            muscleMassTrend = healthMetricService.analyzeMetricTrend(memberId, MetricType.MUSCLE_MASS, days);
        } catch (Exception e) {
            log.debug("Could not analyze muscle mass trend: {}", e.getMessage());
        }

        return new MetricTrends(weightTrend, bodyFatTrend, muscleMassTrend);
    }

    // Helper methods

    private String getTimeOfDay(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        if (hour < 12) {
            return "Morning";
        } else if (hour < 17) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }

    private List<String> generateWorkoutRecommendations(
            int totalWorkouts,
            double avgWorkoutsPerWeek,
            Map<WorkoutIntensity, Long> intensityDistribution,
            int days
    ) {
        List<String> recommendations = new java.util.ArrayList<>();

        // Frequency recommendations
        if (avgWorkoutsPerWeek < 3) {
            recommendations.add("Try to increase workout frequency to at least 3 times per week for optimal results");
        } else if (avgWorkoutsPerWeek > 6) {
            recommendations.add("Consider adding rest days to allow for recovery and prevent overtraining");
        }

        // Intensity recommendations
        long highIntensityCount = intensityDistribution.getOrDefault(WorkoutIntensity.HIGH, 0L) +
                                  intensityDistribution.getOrDefault(WorkoutIntensity.VERY_HIGH, 0L);

        if (totalWorkouts > 0 && highIntensityCount == 0) {
            recommendations.add("Consider adding high-intensity workouts to challenge yourself and improve fitness");
        }

        // Consistency recommendations
        if (totalWorkouts > 0) {
            double expectedWorkouts = (days / 7.0) * 3; // Expect at least 3 per week
            if (totalWorkouts < expectedWorkouts * 0.7) {
                recommendations.add("Focus on building consistency in your workout routine");
            }
        }

        return recommendations;
    }

    private List<String> generateHealthRecommendations(
            MetricTrends trends,
            List<FitnessGoal> activeGoals
    ) {
        List<String> recommendations = new java.util.ArrayList<>();

        // Weight recommendations
        if (trends.weightTrend() != null) {
            HealthMetricService.TrendDirection direction = trends.weightTrend().direction();
            if (direction == HealthMetricService.TrendDirection.INCREASING) {
                boolean hasWeightLossGoal = activeGoals.stream()
                    .anyMatch(g -> g.getGoalType() == GoalType.WEIGHT_LOSS);

                if (hasWeightLossGoal) {
                    recommendations.add("Your weight is trending upward. Review your nutrition and increase cardio activity");
                }
            }
        }

        // Body fat recommendations
        if (trends.bodyFatTrend() != null) {
            HealthMetricService.TrendDirection direction = trends.bodyFatTrend().direction();
            if (direction == HealthMetricService.TrendDirection.INCREASING) {
                recommendations.add("Consider incorporating more strength training to build muscle and reduce body fat");
            }
        }

        // Goal-based recommendations
        long overdueGoals = activeGoals.stream()
            .filter(FitnessGoal::isOverdue)
            .count();

        if (overdueGoals > 0) {
            recommendations.add("You have " + overdueGoals + " overdue goal(s). Consider reviewing and adjusting your targets");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Keep up the great work! Your health metrics are trending positively");
        }

        return recommendations;
    }

    private List<String> generateHealthWarnings(
            MetricTrends trends,
            List<FitnessGoal> activeGoals
    ) {
        List<String> warnings = new java.util.ArrayList<>();

        // Check for rapid weight changes
        if (trends.weightTrend() != null) {
            BigDecimal percentChange = trends.weightTrend().percentageChange().abs();
            if (percentChange.compareTo(BigDecimal.valueOf(10)) > 0) {
                warnings.add("Significant weight change detected (>" + percentChange.intValue() + "%). Consider consulting with a healthcare professional");
            }
        }

        return warnings;
    }

    // DTOs

    public record MemberHealthDashboard(
        List<WorkoutLog> recentWorkouts,
        WorkoutTrackingService.WorkoutStatistics workoutStats,
        HealthMetricService.BodyCompositionSnapshot bodyComposition,
        List<GoalWithProgress> activeGoals,
        int workoutStreak,
        MetricTrends recentTrends,
        FitnessGoalService.GoalStatistics goalStatistics
    ) {}

    public record GoalWithProgress(
        FitnessGoal goal,
        BigDecimal progressPercentage,
        boolean isOverdue,
        long daysRemaining
    ) {}

    public record MetricTrends(
        HealthMetricService.MetricTrend weightTrend,
        HealthMetricService.MetricTrend bodyFatTrend,
        HealthMetricService.MetricTrend muscleMassTrend
    ) {}

    public record WorkoutInsights(
        int totalWorkouts,
        int totalMinutes,
        int totalCalories,
        double avgWorkoutsPerWeek,
        Map<String, Long> timeOfDayDistribution,
        Map<WorkoutIntensity, Long> intensityDistribution,
        List<String> recommendations
    ) {}

    public record HealthInsights(
        MetricTrends trends,
        List<String> recommendations,
        List<String> warnings,
        LocalDateTime generatedAt
    ) {}
}
