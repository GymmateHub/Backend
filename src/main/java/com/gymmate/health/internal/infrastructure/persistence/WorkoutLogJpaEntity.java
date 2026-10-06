package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.domain.enums.WorkoutIntensity;
import com.gymmate.health.internal.domain.enums.WorkoutStatus;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.health.internal.domain.WorkoutLog;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link WorkoutLog} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "WorkoutLog")
@Table(name = "workout_logs", indexes = { @Index(name = "idx_workout_member_date", columnList = "member_id,workout_date"), @Index(name = "idx_workout_gym", columnList = "gym_id,workout_date") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(WorkoutLog.class)
public class WorkoutLogJpaEntity extends GymScopedJpaEntity {

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
    private WorkoutStatus status = WorkoutStatus.COMPLETED;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;
}
