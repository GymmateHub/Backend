package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * MaintenanceSchedule entity representing scheduled maintenance for equipment.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "maintenance_schedules")
public class MaintenanceSchedule extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(name = "equipment_id", nullable = false)
  private UUID equipmentId;

  @Column(name = "schedule_name", nullable = false, length = 200)
  private String scheduleName;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "scheduled_date", nullable = false)
  private LocalDate scheduledDate;

  @Column(name = "maintenance_type", nullable = false, length = 50)
  private String maintenanceType; // routine, inspection, deep_clean, calibration

  @Column(name = "assigned_to", length = 200)
  private String assignedTo; // Staff member or company

  @Column(name = "estimated_duration_hours")
  private Integer estimatedDurationHours;

  @Column(name = "is_recurring")
  @Builder.Default
  private boolean recurring = false;

  @Column(name = "recurrence_interval_days")
  private Integer recurrenceIntervalDays; // For recurring schedules

  @Column(name = "is_completed")
  @Builder.Default
  private boolean completed = false;

  @Column(name = "completed_date")
  private LocalDate completedDate;

  @Column(name = "maintenance_record_id")
  private UUID maintenanceRecordId; // Link to actual maintenance record once completed

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "reminder_sent")
  @Builder.Default
  private boolean reminderSent = false;

  @Column(name = "reminder_date")
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
