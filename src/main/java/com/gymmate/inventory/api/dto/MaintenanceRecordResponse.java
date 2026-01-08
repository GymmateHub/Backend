package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.MaintenanceRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for maintenance record response.
 */
public record MaintenanceRecordResponse(
  UUID id,
  UUID equipmentId,
  LocalDate maintenanceDate,
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
  String completionNotes,
  UUID gymId,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static MaintenanceRecordResponse fromEntity(MaintenanceRecord record) {
    return new MaintenanceRecordResponse(
      record.getId(),
      record.getEquipmentId(),
      record.getMaintenanceDate(),
      record.getMaintenanceType(),
      record.getDescription(),
      record.getPerformedBy(),
      record.getTechnicianCompany(),
      record.getCost(),
      record.getPartsReplaced(),
      record.getNextMaintenanceDue(),
      record.getNotes(),
      record.getInvoiceNumber(),
      record.getInvoiceUrl(),
      record.isCompleted(),
      record.getCompletionNotes(),
      record.getGymId(),
      record.getOrganisationId(),
      record.getCreatedAt(),
      record.getUpdatedAt()
    );
  }
}
