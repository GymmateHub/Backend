package com.gymmate.health.internal.domain;

import com.gymmate.health.internal.domain.enums.WorkoutIntensity;
import com.gymmate.health.internal.domain.enums.WorkoutStatus;
import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WorkoutLog entity representing a member's workout session.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 * Implements FR-014: Workout Logging.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class WorkoutLog extends GymScopedEntity {

    private UUID memberId;

    private LocalDateTime workoutDate;

    private String workoutName; // Optional name (e.g., "Chest Day", "Leg Day")

    private Integer durationMinutes;

    private Integer totalCaloriesBurned;

    private 
    WorkoutIntensity intensityLevel;

    private String notes;

    @Builder.Default
    private WorkoutStatus status = WorkoutStatus.COMPLETED;

    private UUID recordedByUserId;

    // Business methods
    public void complete() {
        if (this.status == WorkoutStatus.COMPLETED) {
            throw new DomainException("ALREADY_COMPLETED", "Workout is already completed");
        }
        this.status = WorkoutStatus.COMPLETED;
    }

    public void skip(String reason) {
        if (this.status == WorkoutStatus.COMPLETED) {
            throw new DomainException("CANNOT_SKIP", "Cannot skip a completed workout");
        }
        this.status = WorkoutStatus.SKIPPED;
        if (this.notes == null) {
            this.notes = reason;
        } else {
            this.notes += "\nSkipped: " + reason;
        }
    }

    public boolean isCompleted() {
        return status == WorkoutStatus.COMPLETED;
    }

    public boolean isPlanned() {
        return status == WorkoutStatus.PLANNED;
    }

    public void validateDuration() {
        if (durationMinutes != null && durationMinutes < 0) {
            throw new DomainException("INVALID_DURATION", "Duration must be positive");
        }
    }

    public void validateCalories() {
        if (totalCaloriesBurned != null && totalCaloriesBurned < 0) {
            throw new DomainException("INVALID_CALORIES", "Calories must be positive");
        }
    }
}
