package com.gymmate.health.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import com.gymmate.shared.exception.DomainException;
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
@EqualsAndHashCode(callSuper = true)
@Builder
public class WorkoutExercise extends BaseAuditEntity {

    private UUID workoutLogId;

    private UUID exerciseId;

    private Integer exerciseOrder; // Order in workout sequence

    @Builder.Default
    private 
    Integer sets = 1;

    @Builder.Default
    private 
    Integer reps = 1;

    private BigDecimal weight;

    private String weightUnit; // kg, lbs

    private Integer restSeconds;

    private BigDecimal distanceMeters; // For cardio exercises

    private Integer durationSeconds; // For timed exercises

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
