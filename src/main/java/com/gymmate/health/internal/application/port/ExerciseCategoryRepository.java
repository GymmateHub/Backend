package com.gymmate.health.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.health.internal.domain.ExerciseCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ExerciseCategory.
 * Defines domain-level operations for managing exercise categories.
 */
public interface ExerciseCategoryRepository extends DomainRepository<ExerciseCategory, UUID> {

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

    List<ExerciseCategory> findAllActiveOrderByDisplayOrder();

    Optional<ExerciseCategory> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
