package com.gymmate.health.api;

import com.gymmate.health.api.dto.*;
import com.gymmate.health.application.WorkoutTrackingService;
import com.gymmate.health.domain.WorkoutLog;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for workout logging and tracking.
 * Implements FR-014: Workout Logging.
 */
@Slf4j
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workout Tracking", description = "Workout Logging & Tracking APIs")
public class WorkoutController {

    private final WorkoutTrackingService workoutTrackingService;

    /**
     * Log a new workout with exercises.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Log workout", description = "Log a new workout with exercises")
    public ResponseEntity<ApiResponse<WorkoutLogResponse>> logWorkout(
            @RequestParam UUID gymId,
            @Valid @RequestBody LogWorkoutRequest request) {

        UUID organisationId = TenantContext.getCurrentTenantId();

        // Convert request exercises to service DTOs
        List<WorkoutTrackingService.WorkoutExerciseDetail> exerciseDetails = request.exercises().stream()
            .map(ex -> new WorkoutTrackingService.WorkoutExerciseDetail(
                ex.exerciseId(),
                ex.exerciseOrder(),
                ex.sets(),
                ex.reps(),
                ex.weight(),
                ex.weightUnit(),
                ex.restSeconds(),
                ex.distanceMeters(),
                ex.durationSeconds(),
                ex.notes()
            ))
            .toList();

        WorkoutLog workout = workoutTrackingService.logWorkout(
            organisationId,
            gymId,
            request.memberId(),
            request.workoutDate(),
            request.workoutName(),
            request.durationMinutes(),
            request.totalCaloriesBurned(),
            request.intensityLevel(),
            request.notes(),
            exerciseDetails
        );

        // Fetch the saved exercises for the response
        WorkoutTrackingService.WorkoutWithExercises workoutWithExercises =
            workoutTrackingService.getWorkoutById(workout.getId());

        List<WorkoutExerciseResponse> exerciseResponses = workoutWithExercises.exercises().stream()
            .map(WorkoutExerciseResponse::from)
            .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                WorkoutLogResponse.from(workout, exerciseResponses),
                "Workout logged successfully"
            ));
    }

    /**
     * Get workout by ID with exercises.
     */
    @GetMapping("/{workoutId}")
    @Operation(summary = "Get workout by ID", description = "Retrieve a specific workout with all exercises")
    public ResponseEntity<ApiResponse<WorkoutLogResponse>> getWorkoutById(@PathVariable UUID workoutId) {
        WorkoutTrackingService.WorkoutWithExercises workoutWithExercises =
            workoutTrackingService.getWorkoutById(workoutId);

        List<WorkoutExerciseResponse> exerciseResponses = workoutWithExercises.exercises().stream()
            .map(WorkoutExerciseResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(
            WorkoutLogResponse.from(workoutWithExercises.workout(), exerciseResponses)
        ));
    }

    /**
     * Get workout history for a member.
     */
    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member workout history", description = "Retrieve all workouts for a member")
    public ResponseEntity<ApiResponse<List<WorkoutLogResponse>>> getWorkoutHistory(@PathVariable UUID memberId) {
        List<WorkoutLog> workouts = workoutTrackingService.getWorkoutHistory(memberId);

        List<WorkoutLogResponse> responses = workouts.stream()
            .map(workout -> {
                WorkoutTrackingService.WorkoutWithExercises details =
                    workoutTrackingService.getWorkoutById(workout.getId());
                List<WorkoutExerciseResponse> exerciseResponses = details.exercises().stream()
                    .map(WorkoutExerciseResponse::from)
                    .toList();
                return WorkoutLogResponse.from(workout, exerciseResponses);
            })
            .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get workout history by date range.
     */
    @GetMapping("/member/{memberId}/range")
    @Operation(summary = "Get workouts by date range", description = "Retrieve workouts within a date range")
    public ResponseEntity<ApiResponse<List<WorkoutLogResponse>>> getWorkoutHistoryByDateRange(
            @PathVariable UUID memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<WorkoutLog> workouts = workoutTrackingService.getWorkoutHistoryByDateRange(
            memberId, startDate, endDate
        );

        List<WorkoutLogResponse> responses = workouts.stream()
            .map(workout -> {
                WorkoutTrackingService.WorkoutWithExercises details =
                    workoutTrackingService.getWorkoutById(workout.getId());
                List<WorkoutExerciseResponse> exerciseResponses = details.exercises().stream()
                    .map(WorkoutExerciseResponse::from)
                    .toList();
                return WorkoutLogResponse.from(workout, exerciseResponses);
            })
            .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get paginated workout history.
     */
    @GetMapping("/member/{memberId}/paginated")
    @Operation(summary = "Get paginated workouts", description = "Retrieve paginated workout history")
    public ResponseEntity<ApiResponse<Page<WorkoutLogResponse>>> getWorkoutHistoryPaginated(
            @PathVariable UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<WorkoutLog> workoutsPage = workoutTrackingService.getWorkoutHistoryPaginated(memberId, page, size);

        Page<WorkoutLogResponse> responsePage = workoutsPage.map(workout -> {
            WorkoutTrackingService.WorkoutWithExercises details =
                workoutTrackingService.getWorkoutById(workout.getId());
            List<WorkoutExerciseResponse> exerciseResponses = details.exercises().stream()
                .map(WorkoutExerciseResponse::from)
                .toList();
            return WorkoutLogResponse.from(workout, exerciseResponses);
        });

        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }

    /**
     * Get latest workout for a member.
     */
    @GetMapping("/member/{memberId}/latest")
    @Operation(summary = "Get latest workout", description = "Retrieve the most recent workout for a member")
    public ResponseEntity<ApiResponse<WorkoutLogResponse>> getLatestWorkout(@PathVariable UUID memberId) {
        WorkoutLog workout = workoutTrackingService.getLatestWorkout(memberId);
        if (workout == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No workouts found"));
        }

        WorkoutTrackingService.WorkoutWithExercises details =
            workoutTrackingService.getWorkoutById(workout.getId());
        List<WorkoutExerciseResponse> exerciseResponses = details.exercises().stream()
            .map(WorkoutExerciseResponse::from)
            .toList();

        return ResponseEntity.ok(ApiResponse.success(WorkoutLogResponse.from(workout, exerciseResponses)));
    }

    /**
     * Calculate workout statistics.
     */
    @GetMapping("/member/{memberId}/statistics")
    @Operation(summary = "Get workout statistics", description = "Calculate workout statistics within a date range")
    public ResponseEntity<ApiResponse<WorkoutStatisticsResponse>> calculateStatistics(
            @PathVariable UUID memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        WorkoutTrackingService.WorkoutStatistics stats =
            workoutTrackingService.calculateStatistics(memberId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(WorkoutStatisticsResponse.from(stats)));
    }

    /**
     * Get workout streak.
     */
    @GetMapping("/member/{memberId}/streak")
    @Operation(summary = "Get workout streak", description = "Calculate consecutive days with workouts")
    public ResponseEntity<ApiResponse<Integer>> getWorkoutStreak(@PathVariable UUID memberId) {
        int streak = workoutTrackingService.calculateWorkoutStreak(memberId);
        return ResponseEntity.ok(ApiResponse.success(streak));
    }

    /**
     * Delete a workout.
     */
    @DeleteMapping("/{workoutId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Delete workout", description = "Delete a workout and its exercises")
    public ResponseEntity<ApiResponse<Void>> deleteWorkout(@PathVariable UUID workoutId) {
        workoutTrackingService.deleteWorkout(workoutId);
        return ResponseEntity.ok(ApiResponse.success(null, "Workout deleted successfully"));
    }
}

