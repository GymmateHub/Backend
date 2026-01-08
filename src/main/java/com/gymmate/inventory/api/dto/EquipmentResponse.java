package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentCategory;
import com.gymmate.inventory.domain.EquipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for equipment responses.
 */
public record EquipmentResponse(
  UUID id,
  String name,
  EquipmentCategory category,
  String description,
  String manufacturer,
  String model,
  String serialNumber,
  EquipmentStatus status,
  LocalDate purchaseDate,
  BigDecimal purchasePrice,
  BigDecimal currentValue,
  LocalDate warrantyExpiryDate,
  String warrantyProvider,
  UUID areaId,
  String locationNotes,
  LocalDate lastMaintenanceDate,
  LocalDate nextMaintenanceDate,
  Integer maintenanceIntervalDays,
  BigDecimal totalMaintenanceCost,
  Integer usageHours,
  Integer maxCapacity,
  UUID supplierId,
  String imageUrl,
  String notes,
  UUID gymId,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  boolean active
) {
  public static EquipmentResponse fromEntity(Equipment equipment) {
    return new EquipmentResponse(
      equipment.getId(),
      equipment.getName(),
      equipment.getCategory(),
      equipment.getDescription(),
      equipment.getManufacturer(),
      equipment.getModel(),
      equipment.getSerialNumber(),
      equipment.getStatus(),
      equipment.getPurchaseDate(),
      equipment.getPurchasePrice(),
      equipment.getCurrentValue(),
      equipment.getWarrantyExpiryDate(),
      equipment.getWarrantyProvider(),
      equipment.getAreaId(),
      equipment.getLocationNotes(),
      equipment.getLastMaintenanceDate(),
      equipment.getNextMaintenanceDate(),
      equipment.getMaintenanceIntervalDays(),
      equipment.getTotalMaintenanceCost(),
      equipment.getUsageHours(),
      equipment.getMaxCapacity(),
      equipment.getSupplierId(),
      equipment.getImageUrl(),
      equipment.getNotes(),
      equipment.getGymId(),
      equipment.getOrganisationId(),
      equipment.getCreatedAt(),
      equipment.getUpdatedAt(),
      equipment.isActive()
    );
  }
}
