package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.ExerciseCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ExerciseCategory.
 * Defines domain-level operations for managing exercise categories.
 */
public interface ExerciseCategoryRepository {

    /**
     * Save or update an exercise category.
     */
    ExerciseCategory save(ExerciseCategory category);

    /**
     * Find category by ID.
     */
    Optional<ExerciseCategory> findById(UUID id);

    /**
     * Find all active categories ordered by display order.
     */
    List<ExerciseCategory> findAllActive();

    /**
     * Find category by name.
     */
    Optional<ExerciseCategory> findByName(String name);

    /**
     * Delete a category (soft delete by setting active=false).
     */
    void delete(ExerciseCategory category);

    /**
     * Check if category name exists.
     */
    boolean existsByName(String name);
}
