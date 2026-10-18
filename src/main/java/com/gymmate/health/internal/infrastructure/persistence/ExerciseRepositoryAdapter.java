package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.ExerciseRepository;
import com.gymmate.health.internal.domain.Exercise;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ExerciseRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the Exercise finders live here.
 */
@Component
@Transactional()
public class ExerciseRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<Exercise, UUID, ExerciseJpaRepository>
        implements ExerciseRepository {

    public ExerciseRepositoryAdapter(ExerciseJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<Exercise> findAllPublicExercises() {
        return this.<List<Exercise>>fromJpa(jpaRepository.findAllPublicExercises());
    }

    @Override
    public List<Exercise> findByCategory(UUID categoryId) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<Exercise> findByMuscleGroup(String muscleGroup) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByPrimaryMuscleGroup(muscleGroup));
    }

    @Override
    public List<Exercise> findByDifficultyLevel(String difficultyLevel) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByDifficultyLevel(difficultyLevel));
    }

    @Override
    public List<Exercise> findByGymId(UUID gymId) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByCreatedByGymId(gymId));
    }

    @Override
    public List<Exercise> findAvailableForGym(UUID gymId) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findAvailableForGym(gymId));
    }

    @Override
    public List<Exercise> searchByName(String searchTerm) {
        return this.<List<Exercise>>fromJpa(jpaRepository.searchByName(searchTerm));
    }

    @Override
    public boolean existsByNameAndGymId(String name, UUID gymId) {
        return jpaRepository.existsByNameAndGymId(name, gymId);
    }

    @Override
    public List<Exercise> findByCategoryId(UUID categoryId) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByCategoryId(categoryId));
    }

    @Override
    public List<Exercise> findByPrimaryMuscleGroup(String muscleGroup) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByPrimaryMuscleGroup(muscleGroup));
    }

    @Override
    public List<Exercise> findByCreatedByGymId(UUID gymId) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findByCreatedByGymId(gymId));
    }
}
