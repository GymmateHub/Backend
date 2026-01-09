package com.gymmate.health.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * FitnessGoal entity representing member fitness goals with progress tracking.
 * Supports various goal types: weight loss, muscle gain, strength, endurance, etc.
 * Implements FR-015: Health Insights & Goals.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "fitness_goals", indexes = {
    @Index(name = "idx_goal_member_status", columnList = "member_id,status"),
    @Index(name = "idx_goal_deadline", columnList = "deadline_date")
})
public class FitnessGoal extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "target_value", precision = 10, scale = 2)
    private BigDecimal targetValue;

    @Column(name = "target_unit", length = 20)
    private String targetUnit;

    @Column(name = "start_value", precision = 10, scale = 2)
    private BigDecimal startValue;

    @Column(name = "current_value", precision = 10, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Column(name = "achieved_date")
    private LocalDate achievedDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    // Business methods

    /**
     * Mark goal as achieved.
     */
    public void achieve() {
        if (this.status == GoalStatus.ACHIEVED) {
            throw new DomainException("ALREADY_ACHIEVED", "Goal is already achieved");
        }
        this.status = GoalStatus.ACHIEVED;
        this.achievedDate = LocalDate.now();
    }

    /**
     * Abandon the goal with a reason.
     */
    public void abandon(String reason) {
        if (this.status == GoalStatus.ACHIEVED) {
            throw new DomainException("CANNOT_ABANDON", "Cannot abandon an achieved goal");
        }
        this.status = GoalStatus.ABANDONED;
        if (this.description == null) {
            this.description = "Abandoned: " + reason;
        } else {
            this.description += "\n\nAbandoned: " + reason;
        }
    }

    /**
     * Put goal on hold temporarily.
     */
    public void pauseGoal() {
        if (this.status != GoalStatus.ACTIVE) {
            throw new DomainException("CANNOT_PAUSE", "Only active goals can be paused");
        }
        this.status = GoalStatus.ON_HOLD;
    }

    /**
     * Resume a paused goal.
     */
    public void resumeGoal() {
        if (this.status != GoalStatus.ON_HOLD) {
            throw new DomainException("CANNOT_RESUME", "Only paused goals can be resumed");
        }
        this.status = GoalStatus.ACTIVE;
    }

    /**
     * Update current progress value.
     */
    public void updateProgress(BigDecimal newValue) {
        if (newValue == null || newValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("INVALID_PROGRESS", "Progress value must be positive");
        }
        this.currentValue = newValue;

        // Auto-achieve if target reached
        if (targetValue != null && isTargetReached()) {
            achieve();
        }
    }

    /**
     * Calculate progress percentage.
     */
    public BigDecimal calculateProgress() {
        if (startValue == null || targetValue == null || currentValue == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal range = targetValue.subtract(startValue);
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal progress = currentValue.subtract(startValue);
        return progress.divide(range, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Check if deadline has passed.
     */
    public boolean isOverdue() {
        return deadlineDate != null &&
               LocalDate.now().isAfter(deadlineDate) &&
               status == GoalStatus.ACTIVE;
    }

    /**
     * Check if target value has been reached.
     */
    public boolean isTargetReached() {
        if (targetValue == null || currentValue == null) {
            return false;
        }

        // For weight loss goals, target is less than start
        if (goalType == GoalType.WEIGHT_LOSS || goalType == GoalType.BODY_FAT_REDUCTION) {
            return currentValue.compareTo(targetValue) <= 0;
        }

        // For gain/improvement goals, target is more than start
        return currentValue.compareTo(targetValue) >= 0;
    }

    /**
     * Get days remaining until deadline.
     */
    public long getDaysRemaining() {
        if (deadlineDate == null) {
            return -1;
        }
        return LocalDate.now().until(deadlineDate).getDays();
    }

    /**
     * Validate goal data.
     */
    public void validate() {
        if (title == null || title.isBlank()) {
            throw new DomainException("INVALID_TITLE", "Goal title is required");
        }
        if (startDate == null) {
            throw new DomainException("INVALID_START_DATE", "Start date is required");
        }
        if (deadlineDate != null && deadlineDate.isBefore(startDate)) {
            throw new DomainException("INVALID_DEADLINE", "Deadline cannot be before start date");
        }
    }
}
