package com.gymmate.health.application;

import com.gymmate.health.domain.*;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for managing exercise library.
 * Handles exercise and category operations including public and gym-specific exercises.
 * Implements FR-013: Exercise Library.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository categoryRepository;

    /**
     * Get all exercise categories.
     */
    @Transactional(readOnly = true)
    public List<ExerciseCategory> getAllCategories() {
        log.debug("Fetching all active exercise categories");
        return categoryRepository.findAllActive();
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    public ExerciseCategory getCategoryById(UUID categoryId) {
        log.debug("Fetching exercise category: {}", categoryId);
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with ID: " + categoryId));
    }

    /**
     * Create new exercise category (admin only).
     */
    @Transactional
    public ExerciseCategory createCategory(String name, String description, String iconUrl, Integer displayOrder) {
        log.info("Creating new exercise category: {}", name);

        if (categoryRepository.existsByName(name)) {
            throw new DomainException("CATEGORY_EXISTS", "Exercise category already exists with name: " + name);
        }

        ExerciseCategory category = ExerciseCategory.builder()
            .name(name)
            .description(description)
            .iconUrl(iconUrl)
            .displayOrder(displayOrder)
            .build();

        return categoryRepository.save(category);
    }

    /**
     * Get all public exercises (available to all gyms).
     */
    @Transactional(readOnly = true)
    public List<Exercise> getAllPublicExercises() {
        log.debug("Fetching all public exercises");
        return exerciseRepository.findAllPublicExercises();
    }

    /**
     * Get all exercises available to a gym (public + gym-specific).
     */
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesForGym(UUID gymId) {
        log.debug("Fetching exercises available for gym: {}", gymId);
        return exerciseRepository.findAvailableForGym(gymId);
    }

    /**
     * Get exercises by category.
     */
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByCategory(UUID categoryId) {
        log.debug("Fetching exercises for category: {}", categoryId);

        // Verify category exists
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with ID: " + categoryId));

        return exerciseRepository.findByCategory(categoryId);
    }

    /**
     * Get exercises by muscle group.
     */
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByMuscleGroup(String muscleGroup) {
        log.debug("Fetching exercises for muscle group: {}", muscleGroup);
        return exerciseRepository.findByMuscleGroup(muscleGroup);
    }

    /**
     * Get exercises by difficulty level.
     */
    @Transactional(readOnly = true)
    public List<Exercise> getExercisesByDifficulty(String difficultyLevel) {
        log.debug("Fetching exercises for difficulty level: {}", difficultyLevel);
        return exerciseRepository.findByDifficultyLevel(difficultyLevel);
    }

    /**
     * Search exercises by name.
     */
    @Transactional(readOnly = true)
    public List<Exercise> searchExercises(String searchTerm) {
        log.debug("Searching exercises with term: {}", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new DomainException("INVALID_SEARCH", "Search term cannot be empty");
        }
        return exerciseRepository.searchByName(searchTerm.trim());
    }

    /**
     * Get exercise by ID.
     */
    @Transactional(readOnly = true)
    public Exercise getExerciseById(UUID exerciseId) {
        log.debug("Fetching exercise: {}", exerciseId);
        return exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with ID: " + exerciseId));
    }

    /**
     * Create custom gym-specific exercise.
     */
    @Transactional
    public Exercise createCustomExercise(
            UUID gymId,
            String name,
            String description,
            UUID categoryId,
            String primaryMuscleGroup,
            String[] secondaryMuscleGroups,
            String equipmentRequired,
            String difficultyLevel,
            String instructions,
            String videoUrl
    ) {
        log.info("Creating custom exercise for gym {}: {}", gymId, name);

        // Verify category exists
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with ID: " + categoryId));

        // Check if gym already has an exercise with this name
        if (exerciseRepository.existsByNameAndGymId(name, gymId)) {
            throw new DomainException("EXERCISE_EXISTS", "Custom exercise already exists with name: " + name);
        }

        // Validate difficulty level
        if (difficultyLevel != null &&
            !List.of("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(difficultyLevel)) {
            throw new DomainException("INVALID_DIFFICULTY",
                "Difficulty level must be one of: BEGINNER, INTERMEDIATE, ADVANCED");
        }

        Exercise exercise = Exercise.builder()
            .name(name)
            .description(description)
            .categoryId(categoryId)
            .primaryMuscleGroup(primaryMuscleGroup)
            .secondaryMuscleGroups(secondaryMuscleGroups)
            .equipmentRequired(equipmentRequired)
            .difficultyLevel(difficultyLevel)
            .instructions(instructions)
            .videoUrl(videoUrl)
            .isPublic(false)
            .createdByGymId(gymId)
            .build();

        return exerciseRepository.save(exercise);
    }

    /**
     * Update custom exercise (gym can only update their own).
     */
    @Transactional
    public Exercise updateCustomExercise(
            UUID exerciseId,
            UUID gymId,
            String name,
            String description,
            UUID categoryId,
            String primaryMuscleGroup,
            String[] secondaryMuscleGroups,
            String equipmentRequired,
            String difficultyLevel,
            String instructions,
            String videoUrl
    ) {
        log.info("Updating custom exercise {} for gym {}", exerciseId, gymId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with ID: " + exerciseId));

        // Verify this is a custom exercise owned by this gym
        if (exercise.isPublic() || !gymId.equals(exercise.getCreatedByGymId())) {
            throw new DomainException("UNAUTHORIZED",
                "Cannot update this exercise. Only custom gym exercises can be modified by their owner.");
        }

        // Verify category exists if changed
        if (categoryId != null && !categoryId.equals(exercise.getCategoryId())) {
            categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise category not found with ID: " + categoryId));
            exercise.setCategoryId(categoryId);
        }

        // Update fields
        if (name != null) exercise.setName(name);
        if (description != null) exercise.setDescription(description);
        if (primaryMuscleGroup != null) exercise.setPrimaryMuscleGroup(primaryMuscleGroup);
        if (secondaryMuscleGroups != null) exercise.setSecondaryMuscleGroups(secondaryMuscleGroups);
        if (equipmentRequired != null) exercise.setEquipmentRequired(equipmentRequired);
        if (difficultyLevel != null) exercise.setDifficultyLevel(difficultyLevel);
        if (instructions != null) exercise.setInstructions(instructions);
        if (videoUrl != null) exercise.setVideoUrl(videoUrl);

        return exerciseRepository.save(exercise);
    }

    /**
     * Delete custom exercise.
     */
    @Transactional
    public void deleteCustomExercise(UUID exerciseId, UUID gymId) {
        log.info("Deleting custom exercise {} for gym {}", exerciseId, gymId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
            .orElseThrow(() -> new ResourceNotFoundException("Exercise not found with ID: " + exerciseId));

        // Verify this is a custom exercise owned by this gym
        if (exercise.isPublic() || !gymId.equals(exercise.getCreatedByGymId())) {
            throw new DomainException("UNAUTHORIZED",
                "Cannot delete this exercise. Only custom gym exercises can be deleted by their owner.");
        }

        exerciseRepository.delete(exercise);
        log.info("Successfully deleted custom exercise: {}", exerciseId);
    }

    /**
     * Get custom exercises for a gym.
     */
    @Transactional(readOnly = true)
    public List<Exercise> getCustomExercisesForGym(UUID gymId) {
        log.debug("Fetching custom exercises for gym: {}", gymId);
        return exerciseRepository.findByGymId(gymId);
    }
}
