package com.gymmate.health.domain;

import com.gymmate.health.domain.Enums.WorkoutIntensity;
import com.gymmate.health.domain.Enums.WorkoutStatus;
import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * WorkoutLog entity representing a member's workout session.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 * Implements FR-014: Workout Logging.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "workout_logs", indexes = {
    @Index(name = "idx_workout_member_date", columnList = "member_id,workout_date"),
    @Index(name = "idx_workout_gym", columnList = "gym_id,workout_date")
})
public class WorkoutLog extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "workout_date", nullable = false)
    private LocalDateTime workoutDate;

    @Column(name = "workout_name", length = 100)
    private String workoutName; // Optional name (e.g., "Chest Day", "Leg Day")

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "total_calories_burned")
    private Integer totalCaloriesBurned;

    @Enumerated(EnumType.STRING)
    @Column(name = "intensity_level", length = 20)
    private WorkoutIntensity intensityLevel;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private WorkoutStatus status = WorkoutStatus.COMPLETED;

    @Column(name = "recorded_by_user_id")
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
