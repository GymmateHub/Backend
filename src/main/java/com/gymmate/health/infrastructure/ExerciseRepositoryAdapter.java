package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.Exercise;
import com.gymmate.health.domain.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ExerciseRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class ExerciseRepositoryAdapter implements ExerciseRepository {

    private final ExerciseJpaRepository jpaRepository;

    @Override
    public Exercise save(Exercise exercise) {
        return jpaRepository.save(exercise);
    }

    @Override
    public Optional<Exercise> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Exercise> findAllPublicExercises() {
        return jpaRepository.findAllPublicExercises();
    }

    @Override
    public List<Exercise> findByCategory(UUID categoryId) {
        return jpaRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Exercise> findByMuscleGroup(String muscleGroup) {
        return jpaRepository.findByPrimaryMuscleGroup(muscleGroup);
    }

    @Override
    public List<Exercise> findByDifficultyLevel(String difficultyLevel) {
        return jpaRepository.findByDifficultyLevel(difficultyLevel);
    }

    @Override
    public List<Exercise> findByGymId(UUID gymId) {
        return jpaRepository.findByCreatedByGymId(gymId);
    }

    @Override
    public List<Exercise> findAvailableForGym(UUID gymId) {
        return jpaRepository.findAvailableForGym(gymId);
    }

    @Override
    public List<Exercise> searchByName(String searchTerm) {
        return jpaRepository.searchByName(searchTerm);
    }

    @Override
    public void delete(Exercise exercise) {
        exercise.setActive(false);
        jpaRepository.save(exercise);
    }

    @Override
    public boolean existsByNameAndGymId(String name, UUID gymId) {
        return jpaRepository.existsByNameAndGymId(name, gymId);
    }
}
