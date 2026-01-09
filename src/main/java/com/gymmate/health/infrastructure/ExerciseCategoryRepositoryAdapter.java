package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.ExerciseCategory;
import com.gymmate.health.domain.ExerciseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ExerciseCategoryRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class ExerciseCategoryRepositoryAdapter implements ExerciseCategoryRepository {

    private final ExerciseCategoryJpaRepository jpaRepository;

    @Override
    public ExerciseCategory save(ExerciseCategory category) {
        return jpaRepository.save(category);
    }

    @Override
    public Optional<ExerciseCategory> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ExerciseCategory> findAllActive() {
        return jpaRepository.findAllActiveOrderByDisplayOrder();
    }

    @Override
    public Optional<ExerciseCategory> findByName(String name) {
        return jpaRepository.findByNameIgnoreCase(name);
    }

    @Override
    public void delete(ExerciseCategory category) {
        category.setActive(false);
        jpaRepository.save(category);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByNameIgnoreCase(name);
    }
}
