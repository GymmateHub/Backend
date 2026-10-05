package com.gymmate.health.internal.application.port;

import com.gymmate.health.internal.domain.Exercise;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    
    List<Exercise> findByCategoryId(UUID categoryId);
    
    List<Exercise> findByPrimaryMuscleGroup(String muscleGroup);
    
    List<Exercise> findByCreatedByGymId(UUID gymId);
    
    List<Exercise> saveAll(Iterable<Exercise> entities);
    
    boolean existsById(UUID id);
    
    List<Exercise> findAll();
    
    List<Exercise> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void deleteAll(Iterable<Exercise> entities);
    
    Exercise saveAndFlush(Exercise entity);
    
    void flush();
    
    Page<Exercise> findAll(Pageable pageable);
}
