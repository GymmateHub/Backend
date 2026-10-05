package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.ExerciseRepository;
import com.gymmate.health.internal.domain.Exercise;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ExerciseRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class ExerciseRepositoryAdapter extends DomainRepositoryAdapter implements ExerciseRepository {

    private final ExerciseJpaRepository jpaRepository;

    public ExerciseRepositoryAdapter(ExerciseJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Exercise save(Exercise exercise) {
        return save(jpaRepository, exercise);
    }

    @Override
    public Optional<Exercise> findById(UUID id) {
        return this.<Optional<Exercise>>fromJpa(jpaRepository.findById(id));
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
    public void delete(Exercise exercise) {
        exercise.setActive(false);
        save(jpaRepository, exercise);
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

    @Override
    public List<Exercise> saveAll(Iterable<Exercise> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Exercise> findAll() {
        return this.<List<Exercise>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Exercise> findAllById(Iterable<UUID> ids) {
        return this.<List<Exercise>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<Exercise> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Exercise saveAndFlush(Exercise entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Exercise> findAll(Pageable pageable) {
        return this.<Page<Exercise>>fromJpa(jpaRepository.findAll(pageable));
    }
}
