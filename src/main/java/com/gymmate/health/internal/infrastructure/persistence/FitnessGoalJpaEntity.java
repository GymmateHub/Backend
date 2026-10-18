package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.domain.enums.GoalStatus;
import com.gymmate.health.internal.domain.enums.GoalType;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.health.internal.domain.FitnessGoal;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link FitnessGoal} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "FitnessGoal")
@Table(name = "fitness_goals", indexes = { @Index(name = "idx_goal_member_status", columnList = "member_id,status"), @Index(name = "idx_goal_deadline", columnList = "deadline_date") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(FitnessGoal.class)
public class // Business methods
FitnessGoalJpaEntity extends GymScopedJpaEntity {

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
    private GoalStatus status = GoalStatus.ACTIVE;
}
