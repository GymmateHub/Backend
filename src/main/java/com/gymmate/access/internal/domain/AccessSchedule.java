package com.gymmate.access.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
public class AccessSchedule extends GymScopedEntity {

  private UUID membershipPlanId;

  /** Day this window applies to; null means every day. */
  private 
  DayOfWeek dayOfWeek;

  private LocalTime startTime;

  private LocalTime endTime;

  public boolean matches(DayOfWeek day, LocalTime time) {
    boolean dayOk = dayOfWeek == null || dayOfWeek == day;
    boolean timeOk = !time.isBefore(startTime) && !time.isAfter(endTime);
    return dayOk && timeOk;
  }
}
