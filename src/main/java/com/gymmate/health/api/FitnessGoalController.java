package com.gymmate.health.api;

import com.gymmate.health.api.dto.*;
import com.gymmate.health.application.FitnessGoalService;
import com.gymmate.health.domain.FitnessGoal;
import com.gymmate.health.domain.Enums.GoalStatus;
import com.gymmate.health.domain.Enums.GoalType;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for fitness goals management.
 * Implements FR-015: Health Insights & Goals (Goal Tracking).
 */
@Slf4j
@RestController
@RequestMapping("/api/fitness-goals")
@RequiredArgsConstructor
@Tag(name = "FitnessGoals", description = "Fitness Goals Management APIs")
public class FitnessGoalController {

    private final FitnessGoalService fitnessGoalService;

    /**
     * Create a new fitness goal.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Create goal", description = "Create a new fitness goal for a member")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> createGoal(
            @RequestParam UUID gymId,
            @Valid @RequestBody CreateGoalRequest request) {

        UUID organisationId = TenantContext.getCurrentTenantId();

        FitnessGoal goal = fitnessGoalService.createGoal(
            organisationId,
            gymId,
            request.memberId(),
            request.goalType(),
            request.title(),
            request.description(),
            request.targetValue(),
            request.targetUnit(),
            request.startValue(),
            request.startDate(),
            request.deadlineDate()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(FitnessGoalResponse.from(goal), "Fitness goal created successfully"));
    }

    /**
     * Get goal by ID.
     */
    @GetMapping("/{goalId}")
    @Operation(summary = "Get goal by ID", description = "Retrieve a specific fitness goal")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> getGoalById(@PathVariable UUID goalId) {
        FitnessGoal goal = fitnessGoalService.getGoalById(goalId);
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal)));
    }

    /**
     * Get all goals for a member.
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member goals", description = "Retrieve all fitness goals for a member")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getMemberGoals(@PathVariable UUID memberId) {
        List<FitnessGoal> goals = fitnessGoalService.getMemberGoals(memberId);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get active goals for a member.
     */
    @GetMapping("/member/{memberId}/active")
    @Operation(summary = "Get active goals", description = "Retrieve active fitness goals for a member")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getActiveGoals(@PathVariable UUID memberId) {
        List<FitnessGoal> goals = fitnessGoalService.getActiveGoals(memberId);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get goals by status.
     */
    @GetMapping("/member/{memberId}/status/{status}")
    @Operation(summary = "Get goals by status", description = "Retrieve goals with a specific status")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getGoalsByStatus(
            @PathVariable UUID memberId,
            @PathVariable GoalStatus status) {

        List<FitnessGoal> goals = fitnessGoalService.getGoalsByStatus(memberId, status);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get goals by type.
     */
    @GetMapping("/member/{memberId}/type/{goalType}")
    @Operation(summary = "Get goals by type", description = "Retrieve goals of a specific type")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getGoalsByType(
            @PathVariable UUID memberId,
            @PathVariable GoalType goalType) {

        List<FitnessGoal> goals = fitnessGoalService.getGoalsByType(memberId, goalType);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Update goal progress.
     */
    @PutMapping("/{goalId}/progress")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Update progress", description = "Update progress for a fitness goal")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> updateGoalProgress(
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalProgressRequest request) {

        FitnessGoal goal = fitnessGoalService.updateGoalProgress(goalId, request.currentValue());
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal), "Goal progress updated successfully"));
    }

    /**
     * Mark goal as achieved.
     */
    @PostMapping("/{goalId}/achieve")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Achieve goal", description = "Manually mark a goal as achieved")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> achieveGoal(@PathVariable UUID goalId) {
        FitnessGoal goal = fitnessGoalService.achieveGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal), "Goal marked as achieved"));
    }

    /**
     * Abandon a goal.
     */
    @PostMapping("/{goalId}/abandon")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Abandon goal", description = "Abandon a fitness goal with a reason")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> abandonGoal(
            @PathVariable UUID goalId,
            @RequestParam(required = false) String reason) {

        FitnessGoal goal = fitnessGoalService.abandonGoal(goalId, reason);
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal), "Goal abandoned"));
    }

    /**
     * Pause a goal.
     */
    @PostMapping("/{goalId}/pause")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Pause goal", description = "Pause an active fitness goal")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> pauseGoal(@PathVariable UUID goalId) {
        FitnessGoal goal = fitnessGoalService.pauseGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal), "Goal paused"));
    }

    /**
     * Resume a paused goal.
     */
    @PostMapping("/{goalId}/resume")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Resume goal", description = "Resume a paused fitness goal")
    public ResponseEntity<ApiResponse<FitnessGoalResponse>> resumeGoal(@PathVariable UUID goalId) {
        FitnessGoal goal = fitnessGoalService.resumeGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success(FitnessGoalResponse.from(goal), "Goal resumed"));
    }

    /**
     * Get goal progress report.
     */
    @GetMapping("/{goalId}/progress-report")
    @Operation(summary = "Get progress report", description = "Get detailed progress report for a goal")
    public ResponseEntity<ApiResponse<GoalProgressReportResponse>> getProgressReport(@PathVariable UUID goalId) {
        FitnessGoalService.GoalProgressReport report = fitnessGoalService.calculateProgressReport(goalId);
        return ResponseEntity.ok(ApiResponse.success(GoalProgressReportResponse.from(report)));
    }

    /**
     * Get goal statistics for a member.
     */
    @GetMapping("/member/{memberId}/statistics")
    @Operation(summary = "Get goal statistics", description = "Get goal statistics for a member")
    public ResponseEntity<ApiResponse<MemberHealthDashboardResponse.GoalStatisticsResponse>> getGoalStatistics(
            @PathVariable UUID memberId) {

        FitnessGoalService.GoalStatistics stats = fitnessGoalService.getMemberGoalStatistics(memberId);
        return ResponseEntity.ok(ApiResponse.success(MemberHealthDashboardResponse.GoalStatisticsResponse.from(stats)));
    }

    /**
     * Get overdue goals for a gym.
     */
    @GetMapping("/gym/{gymId}/overdue")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get overdue goals", description = "Get all overdue goals for a gym")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getOverdueGoals(@PathVariable UUID gymId) {
        List<FitnessGoal> goals = fitnessGoalService.getOverdueGoals(gymId);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get goals with upcoming deadlines.
     */
    @GetMapping("/gym/{gymId}/upcoming-deadlines")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get upcoming deadlines", description = "Get goals with upcoming deadlines for a gym")
    public ResponseEntity<ApiResponse<List<FitnessGoalResponse>>> getUpcomingDeadlines(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "7") int days) {

        List<FitnessGoal> goals = fitnessGoalService.getGoalsWithUpcomingDeadlines(gymId, days);
        List<FitnessGoalResponse> responses = goals.stream()
            .map(FitnessGoalResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Delete a goal.
     */
    @DeleteMapping("/{goalId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Delete goal", description = "Delete a fitness goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable UUID goalId) {
        fitnessGoalService.deleteGoal(goalId);
        return ResponseEntity.ok(ApiResponse.success(null, "Goal deleted successfully"));
    }
}

