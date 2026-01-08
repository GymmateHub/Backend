package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.EquipmentCategory;

import java.util.UUID;

/**
 * DTO for equipment update request.
 */
public record EquipmentUpdateRequest(
  String name,
  EquipmentCategory category,
  String description,
  String manufacturer,
  String model,
  String serialNumber,
  UUID areaId,
  String locationNotes,
  String imageUrl,
  String notes
) {
}
