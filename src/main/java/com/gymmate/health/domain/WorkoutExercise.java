package com.gymmate.health.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * WorkoutExercise entity representing an individual exercise within a workout.
 * Tracks sets, reps, weight, rest times for detailed workout analysis.
 * Implements FR-014: Workout Logging with detailed exercise tracking.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "workout_exercises", indexes = {
    @Index(name = "idx_workout_exercise_log", columnList = "workout_log_id,exercise_order")
})
public class WorkoutExercise extends BaseAuditEntity {

    @Column(name = "workout_log_id", nullable = false)
    private UUID workoutLogId;

    @Column(name = "exercise_id", nullable = false)
    private UUID exerciseId;

    @Column(name = "exercise_order")
    private Integer exerciseOrder; // Order in workout sequence

    @Column(nullable = false)
    @Builder.Default
    private Integer sets = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer reps = 1;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit; // kg, lbs

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "distance_meters", precision = 10, scale = 2)
    private BigDecimal distanceMeters; // For cardio exercises

    @Column(name = "duration_seconds")
    private Integer durationSeconds; // For timed exercises

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Business validation
    public void validate() {
        if (sets == null || sets < 0) {
            throw new DomainException("INVALID_SETS", "Sets must be a positive number");
        }
        if (reps == null || reps < 0) {
            throw new DomainException("INVALID_REPS", "Reps must be a positive number");
        }
        if (weight != null && weight.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("INVALID_WEIGHT", "Weight must be positive");
        }
        if (restSeconds != null && restSeconds < 0) {
            throw new DomainException("INVALID_REST", "Rest time must be positive");
        }
        if (distanceMeters != null && distanceMeters.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("INVALID_DISTANCE", "Distance must be positive");
        }
        if (durationSeconds != null && durationSeconds < 0) {
            throw new DomainException("INVALID_DURATION", "Duration must be positive");
        }
    }

    public BigDecimal calculateVolume() {
        if (weight == null) {
            return BigDecimal.ZERO;
        }
        return weight.multiply(BigDecimal.valueOf(sets * reps));
    }

    public boolean isCardio() {
        return distanceMeters != null || durationSeconds != null;
    }

    public boolean isStrength() {
        return weight != null && weight.compareTo(BigDecimal.ZERO) > 0;
    }
}
