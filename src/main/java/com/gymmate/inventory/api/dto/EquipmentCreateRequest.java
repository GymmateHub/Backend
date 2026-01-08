package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.EquipmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for equipment creation request.
 */
public record EquipmentCreateRequest(
  @NotBlank(message = "Equipment name is required")
  String name,

  @NotNull(message = "Category is required")
  EquipmentCategory category,

  String description,
  String manufacturer,
  String model,
  String serialNumber,
  LocalDate purchaseDate,
  BigDecimal purchasePrice,
  BigDecimal currentValue,
  LocalDate warrantyExpiryDate,
  String warrantyProvider,
  UUID areaId,
  String locationNotes,
  Integer maintenanceIntervalDays,
  Integer maxCapacity,
  UUID supplierId,
  String imageUrl,
  String notes,
  UUID gymId  // Optional: if null, equipment is at org level
) {
}
