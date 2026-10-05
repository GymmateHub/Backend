package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.WorkoutExerciseRepository;
import com.gymmate.health.internal.domain.WorkoutExercise;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;

/**
 * Persistence adapter implementing {@link WorkoutExerciseRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class WorkoutExerciseRepositoryAdapter extends DomainRepositoryAdapter implements WorkoutExerciseRepository {

    private final WorkoutExerciseJpaRepository jpaRepository;

    public WorkoutExerciseRepositoryAdapter(WorkoutExerciseJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkoutExercise save(WorkoutExercise workoutExercise) {
        return save(jpaRepository, workoutExercise);
    }

    @Override
    public List<WorkoutExercise> saveAll(List<WorkoutExercise> workoutExercises) {
        return saveAll(jpaRepository, workoutExercises);
    }

    @Override
    public Optional<WorkoutExercise> findById(UUID id) {
        return this.<Optional<WorkoutExercise>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<WorkoutExercise> findByWorkoutLogId(UUID workoutLogId) {
        return this.<List<WorkoutExercise>>fromJpa(jpaRepository.findByWorkoutLogId(workoutLogId));
    }

    @Override
    public List<WorkoutExercise> findByWorkoutLogIdOrderByExerciseOrder(UUID workoutLogId) {
        return this.<List<WorkoutExercise>>fromJpa(jpaRepository.findByWorkoutLogIdOrderByExerciseOrder(workoutLogId));
    }

    @Override
    public List<WorkoutExercise> findByExerciseId(UUID exerciseId) {
        return this.<List<WorkoutExercise>>fromJpa(jpaRepository.findByExerciseId(exerciseId));
    }

    @Override
    public long countByWorkoutLogId(UUID workoutLogId) {
        return jpaRepository.countByWorkoutLogId(workoutLogId);
    }

    @Override
    public void delete(WorkoutExercise workoutExercise) {
        workoutExercise.setActive(false);
        save(jpaRepository, workoutExercise);
    }

    @Override
    @Transactional
    public void deleteByWorkoutLogId(UUID workoutLogId) {
        jpaRepository.softDeleteByWorkoutLogId(workoutLogId);
    }

    @Override
    public void softDeleteByWorkoutLogId(UUID workoutLogId) {
        jpaRepository.softDeleteByWorkoutLogId(workoutLogId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<WorkoutExercise> findAll() {
        return this.<List<WorkoutExercise>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<WorkoutExercise> findAllById(Iterable<UUID> ids) {
        return this.<List<WorkoutExercise>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<WorkoutExercise> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public WorkoutExercise saveAndFlush(WorkoutExercise entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<WorkoutExercise> findAll(Pageable pageable) {
        return this.<Page<WorkoutExercise>>fromJpa(jpaRepository.findAll(pageable));
    }
}
