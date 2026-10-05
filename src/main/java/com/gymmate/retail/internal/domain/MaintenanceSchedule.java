package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * MaintenanceSchedule entity representing scheduled maintenance for equipment.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MaintenanceSchedule extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private UUID equipmentId;

  private String scheduleName;

  private String description;

  private LocalDate scheduledDate;

  private String maintenanceType; // routine, inspection, deep_clean, calibration

  private String assignedTo; // Staff member or company

  private Integer estimatedDurationHours;

  @Builder.Default
  private boolean recurring = false;

  private Integer recurrenceIntervalDays; // For recurring schedules

  @Builder.Default
  private boolean completed = false;

  private LocalDate completedDate;

  private UUID maintenanceRecordId; // Link to actual maintenance record once completed

  private String notes;

  @Builder.Default
  private boolean reminderSent = false;

  private LocalDate reminderDate;

  // Business methods
  public void complete(UUID maintenanceRecordId) {
    this.completed = true;
    this.completedDate = LocalDate.now();
    this.maintenanceRecordId = maintenanceRecordId;
  }

  public void sendReminder() {
    this.reminderSent = true;
    this.reminderDate = LocalDate.now();
  }

  public boolean isDue() {
    return !completed && LocalDate.now().isAfter(scheduledDate.minusDays(1));
  }

  public boolean isOverdue() {
    return !completed && LocalDate.now().isAfter(scheduledDate);
  }

  public void reschedule(LocalDate newDate) {
    this.scheduledDate = newDate;
    this.reminderSent = false;
  }
}
