package com.gymmate.health.api;

import com.gymmate.health.api.dto.*;
import com.gymmate.health.application.HealthAnalyticsService;
import com.gymmate.health.application.WorkoutTrackingService;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for health analytics and dashboard.
 * Implements FR-015: Health Insights & Goals (Analytics).
 */
@Slf4j
@RestController
@RequestMapping("/api/health-dashboard")
@RequiredArgsConstructor
@Tag(name = "Health Dashboard", description = "Health Analytics & Dashboard APIs")
public class HealthDashboardController {

    private final HealthAnalyticsService healthAnalyticsService;
    private final WorkoutTrackingService workoutTrackingService;

    /**
     * Get comprehensive health dashboard for a member.
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member dashboard", description = "Generate comprehensive health dashboard for a member")
    public ResponseEntity<ApiResponse<MemberHealthDashboardResponse>> getMemberDashboard(@PathVariable UUID memberId) {
        HealthAnalyticsService.MemberHealthDashboard dashboard =
            healthAnalyticsService.generateMemberDashboard(memberId);

        // Convert recent workouts to response DTOs
        List<WorkoutLogResponse> workoutResponses = dashboard.recentWorkouts().stream()
            .map(workout -> {
                WorkoutTrackingService.WorkoutWithExercises details =
                    workoutTrackingService.getWorkoutById(workout.getId());
                List<WorkoutExerciseResponse> exerciseResponses = details.exercises().stream()
                    .map(WorkoutExerciseResponse::from)
                    .toList();
                return WorkoutLogResponse.from(workout, exerciseResponses);
            })
            .toList();

        return ResponseEntity.ok(ApiResponse.success(
            MemberHealthDashboardResponse.from(dashboard, workoutResponses)
        ));
    }

    /**
     * Get workout insights for a member.
     */
    @GetMapping("/member/{memberId}/workout-insights")
    @Operation(summary = "Get workout insights", description = "Generate workout insights and recommendations")
    public ResponseEntity<ApiResponse<WorkoutInsightsResponse>> getWorkoutInsights(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "30") int days) {

        HealthAnalyticsService.WorkoutInsights insights =
            healthAnalyticsService.generateWorkoutInsights(memberId, days);

        return ResponseEntity.ok(ApiResponse.success(WorkoutInsightsResponse.from(insights)));
    }

    /**
     * Get health insights for a member.
     */
    @GetMapping("/member/{memberId}/health-insights")
    @Operation(summary = "Get health insights", description = "Generate health insights and recommendations")
    public ResponseEntity<ApiResponse<HealthInsightsResponse>> getHealthInsights(@PathVariable UUID memberId) {
        HealthAnalyticsService.HealthInsights insights =
            healthAnalyticsService.generateHealthInsights(memberId);

        return ResponseEntity.ok(ApiResponse.success(HealthInsightsResponse.from(insights)));
    }

    /**
     * Analyze metric trends for a member.
     */
    @GetMapping("/member/{memberId}/metric-trends")
    @Operation(summary = "Get metric trends", description = "Analyze key metric trends over time")
    public ResponseEntity<ApiResponse<MemberHealthDashboardResponse.MetricTrendsResponse>> getMetricTrends(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "30") int days) {

        HealthAnalyticsService.MetricTrends trends =
            healthAnalyticsService.analyzeRecentTrends(memberId, days);

        return ResponseEntity.ok(ApiResponse.success(
            MemberHealthDashboardResponse.MetricTrendsResponse.from(trends)
        ));
    }
}

