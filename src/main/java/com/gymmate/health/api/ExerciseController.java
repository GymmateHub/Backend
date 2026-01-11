package com.gymmate.health.api;

import com.gymmate.health.api.dto.CreateExerciseRequest;
import com.gymmate.health.api.dto.ExerciseCategoryResponse;
import com.gymmate.health.api.dto.ExerciseResponse;
import com.gymmate.health.application.ExerciseService;
import com.gymmate.health.domain.Exercise;
import com.gymmate.health.domain.ExerciseCategory;
import com.gymmate.shared.dto.ApiResponse;
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
 * REST controller for exercise library management.
 * Implements FR-013: Exercise Library.
 */
@Slf4j
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise Library", description = "Exercise Library Management APIs")
public class ExerciseController {

    private final ExerciseService exerciseService;

    // ==================== Category Endpoints ====================

    /**
     * Get all exercise categories.
     */
    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Retrieve all active exercise categories")
    public ResponseEntity<ApiResponse<List<ExerciseCategoryResponse>>> getAllCategories() {
        List<ExerciseCategory> categories = exerciseService.getAllCategories();
        List<ExerciseCategoryResponse> responses = categories.stream()
            .map(ExerciseCategoryResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get category by ID.
     */
    @GetMapping("/categories/{categoryId}")
    @Operation(summary = "Get category by ID", description = "Retrieve a specific exercise category")
    public ResponseEntity<ApiResponse<ExerciseCategoryResponse>> getCategoryById(@PathVariable UUID categoryId) {
        ExerciseCategory category = exerciseService.getCategoryById(categoryId);
        return ResponseEntity.ok(ApiResponse.success(ExerciseCategoryResponse.from(category)));
    }

    /**
     * Create new exercise category (admin only).
     */
    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OWNER')")
    @Operation(summary = "Create category", description = "Create a new exercise category (admin only)")
    public ResponseEntity<ApiResponse<ExerciseCategoryResponse>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String iconUrl,
            @RequestParam(required = false) Integer displayOrder) {

        ExerciseCategory category = exerciseService.createCategory(name, description, iconUrl, displayOrder);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ExerciseCategoryResponse.from(category), "Category created successfully"));
    }

    // ==================== Exercise Endpoints ====================

    /**
     * Get all public exercises.
     */
    @GetMapping("/public")
    @Operation(summary = "Get public exercises", description = "Retrieve all public exercises available to all gyms")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getAllPublicExercises() {
        List<Exercise> exercises = exerciseService.getAllPublicExercises();
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get exercises for current gym (public + custom).
     */
    @GetMapping
    @Operation(summary = "Get exercises for gym", description = "Retrieve all exercises available for the current gym")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesForGym(@RequestParam UUID gymId) {
        List<Exercise> exercises = exerciseService.getExercisesForGym(gymId);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get exercise by ID.
     */
    @GetMapping("/{exerciseId}")
    @Operation(summary = "Get exercise by ID", description = "Retrieve a specific exercise")
    public ResponseEntity<ApiResponse<ExerciseResponse>> getExerciseById(@PathVariable UUID exerciseId) {
        Exercise exercise = exerciseService.getExerciseById(exerciseId);
        return ResponseEntity.ok(ApiResponse.success(ExerciseResponse.from(exercise)));
    }

    /**
     * Get exercises by category.
     */
    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Get exercises by category", description = "Retrieve exercises in a specific category")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesByCategory(@PathVariable UUID categoryId) {
        List<Exercise> exercises = exerciseService.getExercisesByCategory(categoryId);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get exercises by muscle group.
     */
    @GetMapping("/by-muscle-group")
    @Operation(summary = "Get exercises by muscle group", description = "Retrieve exercises targeting a specific muscle group")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesByMuscleGroup(@RequestParam String muscleGroup) {
        List<Exercise> exercises = exerciseService.getExercisesByMuscleGroup(muscleGroup);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get exercises by difficulty level.
     */
    @GetMapping("/by-difficulty")
    @Operation(summary = "Get exercises by difficulty", description = "Retrieve exercises by difficulty level")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getExercisesByDifficulty(@RequestParam String difficultyLevel) {
        List<Exercise> exercises = exerciseService.getExercisesByDifficulty(difficultyLevel);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Search exercises by name.
     */
    @GetMapping("/search")
    @Operation(summary = "Search exercises", description = "Search exercises by name")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> searchExercises(@RequestParam String query) {
        List<Exercise> exercises = exerciseService.searchExercises(query);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get custom exercises for a gym.
     */
    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get custom exercises", description = "Retrieve custom exercises for a specific gym")
    public ResponseEntity<ApiResponse<List<ExerciseResponse>>> getCustomExercises(@RequestParam UUID gymId) {
        List<Exercise> exercises = exerciseService.getCustomExercisesForGym(gymId);
        List<ExerciseResponse> responses = exercises.stream()
            .map(ExerciseResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Create custom exercise for gym.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Create custom exercise", description = "Create a custom exercise for the gym")
    public ResponseEntity<ApiResponse<ExerciseResponse>> createCustomExercise(
            @RequestParam UUID gymId,
            @Valid @RequestBody CreateExerciseRequest request) {

        Exercise exercise = exerciseService.createCustomExercise(
            gymId,
            request.name(),
            request.description(),
            request.categoryId(),
            request.primaryMuscleGroup(),
            request.secondaryMuscleGroups(),
            request.equipmentRequired(),
            request.difficultyLevel(),
            request.instructions(),
            request.videoUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(ExerciseResponse.from(exercise), "Custom exercise created successfully"));
    }

    /**
     * Update custom exercise.
     */
    @PutMapping("/{exerciseId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Update custom exercise", description = "Update a custom exercise owned by the gym")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateCustomExercise(
            @PathVariable UUID exerciseId,
            @RequestParam UUID gymId,
            @Valid @RequestBody CreateExerciseRequest request) {

        Exercise exercise = exerciseService.updateCustomExercise(
            exerciseId,
            gymId,
            request.name(),
            request.description(),
            request.categoryId(),
            request.primaryMuscleGroup(),
            request.secondaryMuscleGroups(),
            request.equipmentRequired(),
            request.difficultyLevel(),
            request.instructions(),
            request.videoUrl()
        );

        return ResponseEntity.ok(ApiResponse.success(ExerciseResponse.from(exercise), "Custom exercise updated successfully"));
    }

    /**
     * Delete custom exercise.
     */
    @DeleteMapping("/{exerciseId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Delete custom exercise", description = "Delete a custom exercise owned by the gym")
    public ResponseEntity<ApiResponse<Void>> deleteCustomExercise(
            @PathVariable UUID exerciseId,
            @RequestParam UUID gymId) {

        exerciseService.deleteCustomExercise(exerciseId, gymId);
        return ResponseEntity.ok(ApiResponse.success(null, "Custom exercise deleted successfully"));
    }
}

