package com.gymmate.access.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Allowed entry window for a membership plan. If no schedule rows exist for a
 * plan, entry is allowed at any time. When rows exist, the entry time must fall
 * within at least one window for the current day.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "access_schedules")
public class AccessSchedule extends GymScopedEntity {

  @Column(name = "membership_plan_id", nullable = false)
  private UUID membershipPlanId;

  /** Day this window applies to; null means every day. */
  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", length = 10)
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  public boolean matches(DayOfWeek day, LocalTime time) {
    boolean dayOk = dayOfWeek == null || dayOfWeek == day;
    boolean timeOk = !time.isBefore(startTime) && !time.isAfter(endTime);
    return dayOk && timeOk;
  }
}
