package com.gymmate.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating maintenance schedule.
 */
public record MaintenanceScheduleCreateRequest(
  @NotNull(message = "Equipment ID is required")
  UUID equipmentId,

  @NotBlank(message = "Schedule name is required")
  String scheduleName,

  String description,

  @NotNull(message = "Scheduled date is required")
  LocalDate scheduledDate,

  @NotBlank(message = "Maintenance type is required")
  String maintenanceType,

  String assignedTo,
  Integer estimatedDurationHours,
  boolean recurring,
  Integer recurrenceIntervalDays,
  String notes
) {
}
