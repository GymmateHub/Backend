package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import com.gymmate.access.internal.domain.AccessSchedule;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AccessSchedule} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AccessSchedule")
@Table(name = "access_schedules")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AccessSchedule.class)
public class AccessScheduleJpaEntity extends GymScopedJpaEntity {

    @Column(name = "membership_plan_id", nullable = false)
    private UUID membershipPlanId;

    /**
     * Day this window applies to; null means every day.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
