package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.MaintenanceSchedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for maintenance schedule response.
 */
public record MaintenanceScheduleResponse(
  UUID id,
  UUID equipmentId,
  String scheduleName,
  String description,
  LocalDate scheduledDate,
  String maintenanceType,
  String assignedTo,
  Integer estimatedDurationHours,
  boolean recurring,
  Integer recurrenceIntervalDays,
  boolean completed,
  LocalDate completedDate,
  UUID maintenanceRecordId,
  String notes,
  boolean reminderSent,
  LocalDate reminderDate,
  UUID gymId,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static MaintenanceScheduleResponse fromEntity(MaintenanceSchedule schedule) {
    return new MaintenanceScheduleResponse(
      schedule.getId(),
      schedule.getEquipmentId(),
      schedule.getScheduleName(),
      schedule.getDescription(),
      schedule.getScheduledDate(),
      schedule.getMaintenanceType(),
      schedule.getAssignedTo(),
      schedule.getEstimatedDurationHours(),
      schedule.isRecurring(),
      schedule.getRecurrenceIntervalDays(),
      schedule.isCompleted(),
      schedule.getCompletedDate(),
      schedule.getMaintenanceRecordId(),
      schedule.getNotes(),
      schedule.isReminderSent(),
      schedule.getReminderDate(),
      schedule.getGymId(),
      schedule.getOrganisationId(),
      schedule.getCreatedAt(),
      schedule.getUpdatedAt()
    );
  }
}
