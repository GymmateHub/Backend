package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for Exercise entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface ExerciseJpaRepository extends JpaRepository<Exercise, UUID> {

    /**
     * Find all public exercises ordered by name.
     */
    @Query("SELECT e FROM Exercise e WHERE e.isPublic = true AND e.active = true ORDER BY e.name")
    List<Exercise> findAllPublicExercises();

    /**
     * Find exercises by category.
     */
    @Query("SELECT e FROM Exercise e WHERE e.categoryId = :categoryId AND e.active = true ORDER BY e.name")
    List<Exercise> findByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find exercises by primary muscle group.
     */
    @Query("SELECT e FROM Exercise e WHERE e.primaryMuscleGroup = :muscleGroup AND e.isPublic = true AND e.active = true ORDER BY e.name")
    List<Exercise> findByPrimaryMuscleGroup(@Param("muscleGroup") String muscleGroup);

    /**
     * Find exercises by difficulty level.
     */
    @Query("SELECT e FROM Exercise e WHERE e.difficultyLevel = :level AND e.isPublic = true AND e.active = true ORDER BY e.name")
    List<Exercise> findByDifficultyLevel(@Param("level") String level);

    /**
     * Find custom exercises created by specific gym.
     */
    @Query("SELECT e FROM Exercise e WHERE e.createdByGymId = :gymId AND e.active = true ORDER BY e.name")
    List<Exercise> findByCreatedByGymId(@Param("gymId") UUID gymId);

    /**
     * Find all exercises available to a gym (public + gym-specific).
     */
    @Query("SELECT e FROM Exercise e WHERE e.active = true AND (e.isPublic = true OR e.createdByGymId = :gymId) ORDER BY e.name")
    List<Exercise> findAvailableForGym(@Param("gymId") UUID gymId);

    /**
     * Search exercises by name (case-insensitive partial match).
     */
    @Query("SELECT e FROM Exercise e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND e.active = true ORDER BY e.name")
    List<Exercise> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Check if exercise name exists for a gym's custom exercises.
     */
    @Query("SELECT COUNT(e) > 0 FROM Exercise e WHERE LOWER(e.name) = LOWER(:name) AND e.createdByGymId = :gymId AND e.active = true")
    boolean existsByNameAndGymId(@Param("name") String name, @Param("gymId") UUID gymId);
}
