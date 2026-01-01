package com.gymmate.classes.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ClassSchedule entity representing a scheduled class instance.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "class_schedules")
public class ClassSchedule extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)
  @Column(name = "class_id", nullable = false)
  private UUID classId;

  @Column(name = "trainer_id")
  private UUID trainerId;

  @Column(name = "area_id")
  private UUID areaId;

  // Timing
  @Column(name = "start_time", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalDateTime endTime;

  // Overrides for this specific instance
  @Column(name = "capacity_override")
  private Integer capacityOverride;

  @Column(name = "price_override", precision = 10, scale = 2)
  private BigDecimal priceOverride;

  // Status
  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  @Builder.Default
  private ClassScheduleStatus status = ClassScheduleStatus.SCHEDULED;

  @Column(name = "cancellation_reason", columnDefinition = "TEXT")
  private String cancellationReason;

  // Notes
  @Column(name = "instructor_notes", columnDefinition = "TEXT")
  private String instructorNotes;

  @Column(name = "admin_notes", columnDefinition = "TEXT")
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

