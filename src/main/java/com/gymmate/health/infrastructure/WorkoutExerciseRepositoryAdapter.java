package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WorkoutExercise;
import com.gymmate.health.domain.WorkoutExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing WorkoutExerciseRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class WorkoutExerciseRepositoryAdapter implements WorkoutExerciseRepository {

    private final WorkoutExerciseJpaRepository jpaRepository;

    @Override
    public WorkoutExercise save(WorkoutExercise workoutExercise) {
        return jpaRepository.save(workoutExercise);
    }

    @Override
    public List<WorkoutExercise> saveAll(List<WorkoutExercise> workoutExercises) {
        return jpaRepository.saveAll(workoutExercises);
    }

    @Override
    public Optional<WorkoutExercise> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<WorkoutExercise> findByWorkoutLogId(UUID workoutLogId) {
        return jpaRepository.findByWorkoutLogId(workoutLogId);
    }

    @Override
    public List<WorkoutExercise> findByWorkoutLogIdOrderByExerciseOrder(UUID workoutLogId) {
        return jpaRepository.findByWorkoutLogIdOrderByExerciseOrder(workoutLogId);
    }

    @Override
    public List<WorkoutExercise> findByExerciseId(UUID exerciseId) {
        return jpaRepository.findByExerciseId(exerciseId);
    }

    @Override
    public long countByWorkoutLogId(UUID workoutLogId) {
        return jpaRepository.countByWorkoutLogId(workoutLogId);
    }

    @Override
    public void delete(WorkoutExercise workoutExercise) {
        workoutExercise.setActive(false);
        jpaRepository.save(workoutExercise);
    }

    @Override
    @Transactional
    public void deleteByWorkoutLogId(UUID workoutLogId) {
        jpaRepository.softDeleteByWorkoutLogId(workoutLogId);
    }
}
