package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.WorkoutExerciseRepository;
import com.gymmate.health.internal.domain.WorkoutExercise;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;

/**
 * Persistence adapter implementing {@link WorkoutExerciseRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the WorkoutExercise finders live here.
 */
@Component
@Transactional()
public class WorkoutExerciseRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<WorkoutExercise, UUID, WorkoutExerciseJpaRepository>
        implements WorkoutExerciseRepository {

    public WorkoutExerciseRepositoryAdapter(WorkoutExerciseJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    @Transactional
    public void deleteByWorkoutLogId(UUID workoutLogId) {
        jpaRepository.softDeleteByWorkoutLogId(workoutLogId);
    }

    @Override
    public void softDeleteByWorkoutLogId(UUID workoutLogId) {
        jpaRepository.softDeleteByWorkoutLogId(workoutLogId);
    }
}
