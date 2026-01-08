package com.gymmate.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating maintenance record.
 */
public record MaintenanceRecordCreateRequest(
  @NotNull(message = "Equipment ID is required")
  UUID equipmentId,

  @NotNull(message = "Maintenance date is required")
  LocalDate maintenanceDate,

  @NotBlank(message = "Maintenance type is required")
  String maintenanceType,

  String description,
  String performedBy,
  String technicianCompany,
  BigDecimal cost,
  String partsReplaced,
  LocalDate nextMaintenanceDue,
  String notes,
  String invoiceNumber,
  String invoiceUrl,
  boolean completed,
  String completionNotes
) {
}
