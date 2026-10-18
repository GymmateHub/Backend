package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.constants.ClassScheduleStatus;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ClassSchedule entity representing a scheduled class instance.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ClassSchedule extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
  private UUID classId;

  private UUID trainerId;

  private UUID areaId;

  // Timing
  private LocalDateTime startTime;

  private LocalDateTime endTime;

  // Overrides for this specific instance
  private Integer capacityOverride;

  // Confirmed booking count, kept in sync by an atomic conditional UPDATE in
  // ClassBookingService (see enforce_class_schedule_capacity trigger, V13 migration).
  // Do not set directly outside that path.
  @Builder.Default
  private Integer bookedCount = 0;

  private BigDecimal priceOverride;

  // Status

  @Builder.Default
  private ClassScheduleStatus status = ClassScheduleStatus.SCHEDULED;

  private String cancellationReason;

  // Notes
  private String instructorNotes;

  private String adminNotes;

  public void cancel(String reason) {
    this.status = ClassScheduleStatus.CANCELLED;
    this.cancellationReason = reason;
  }

  public void complete() {
    this.status = ClassScheduleStatus.COMPLETED;
  }

  public void start() {
    this.status = ClassScheduleStatus.IN_PROGRESS;
  }

  public boolean isScheduled() {
    return status == ClassScheduleStatus.SCHEDULED;
  }

  public boolean isCancelled() {
    return status == ClassScheduleStatus.CANCELLED;
  }
}
