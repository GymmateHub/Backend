package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for ExerciseCategory entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface ExerciseCategoryJpaRepository extends JpaRepository<ExerciseCategory, UUID> {

    /**
     * Find all active categories ordered by display order.
     */
    @Query("SELECT ec FROM ExerciseCategory ec WHERE ec.active = true ORDER BY ec.displayOrder ASC")
    List<ExerciseCategory> findAllActiveOrderByDisplayOrder();

    /**
     * Find category by name (case-insensitive).
     */
    @Query("SELECT ec FROM ExerciseCategory ec WHERE LOWER(ec.name) = LOWER(:name) AND ec.active = true")
    Optional<ExerciseCategory> findByNameIgnoreCase(@Param("name") String name);

    /**
     * Check if category exists by name.
     */
    @Query("SELECT COUNT(ec) > 0 FROM ExerciseCategory ec WHERE LOWER(ec.name) = LOWER(:name) AND ec.active = true")
    boolean existsByNameIgnoreCase(@Param("name") String name);
}
