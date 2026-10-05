package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import com.gymmate.health.internal.domain.WorkoutExercise;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link WorkoutExercise} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "WorkoutExercise")
@Table(name = "workout_exercises", indexes = { @Index(name = "idx_workout_exercise_log", columnList = "workout_log_id,exercise_order") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(WorkoutExercise.class)
public class WorkoutExerciseJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "workout_log_id", nullable = false)
    private UUID workoutLogId;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "exercise_order")
    private Integer // Order in workout sequence
    exerciseOrder;

    @Column(nullable = false)
    private Integer sets = 1;

    @Column(nullable = false)
    private Integer reps = 1;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "weight_unit", length = 10)
    private String // kg, lbs
    weightUnit;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "distance_meters", precision = 10, scale = 2)
    private BigDecimal // For cardio exercises
    distanceMeters;

    @Column(name = "duration_seconds")
    private Integer // For timed exercises
    durationSeconds;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
