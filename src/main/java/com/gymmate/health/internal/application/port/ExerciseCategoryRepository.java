package com.gymmate.health.internal.application.port;

import com.gymmate.health.internal.domain.ExerciseCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
    
    List<ExerciseCategory> findAllActiveOrderByDisplayOrder();
    
    Optional<ExerciseCategory> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    List<ExerciseCategory> saveAll(Iterable<ExerciseCategory> entities);
    
    boolean existsById(UUID id);
    
    List<ExerciseCategory> findAll();
    
    List<ExerciseCategory> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void deleteAll(Iterable<ExerciseCategory> entities);
    
    ExerciseCategory saveAndFlush(ExerciseCategory entity);
    
    void flush();
    
    Page<ExerciseCategory> findAll(Pageable pageable);
}
