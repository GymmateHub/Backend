package com.gymmate.health.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Exercise.
 * Defines domain-level operations for managing exercises.
 */
public interface ExerciseRepository {

    /**
     * Save or update an exercise.
     */
    Exercise save(Exercise exercise);

    /**
     * Find exercise by ID.
     */
    Optional<Exercise> findById(UUID id);

    /**
     * Find all public exercises (available to all gyms).
     */
    List<Exercise> findAllPublicExercises();

    /**
     * Find exercises by category.
     */
    List<Exercise> findByCategory(UUID categoryId);

    /**
     * Find exercises by primary muscle group.
     */
    List<Exercise> findByMuscleGroup(String muscleGroup);

    /**
     * Find exercises by difficulty level.
     */
    List<Exercise> findByDifficultyLevel(String difficultyLevel);

    /**
     * Find custom exercises created by a specific gym.
     */
    List<Exercise> findByGymId(UUID gymId);

    /**
     * Find all exercises available to a gym (public + gym-specific).
     */
    List<Exercise> findAvailableForGym(UUID gymId);

    /**
     * Search exercises by name (case-insensitive, partial match).
     */
    List<Exercise> searchByName(String searchTerm);

    /**
     * Delete an exercise (soft delete).
     */
    void delete(Exercise exercise);

    /**
     * Check if exercise name exists for a gym.
     */
    boolean existsByNameAndGymId(String name, UUID gymId);
}
