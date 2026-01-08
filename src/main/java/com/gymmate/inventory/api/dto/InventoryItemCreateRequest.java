package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.InventoryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating inventory item.
 */
public record InventoryItemCreateRequest(
  @NotBlank(message = "Item name is required")
  String name,

  String sku,

  @NotNull(message = "Category is required")
  InventoryCategory category,

  String description,
  Integer minimumStock,
  Integer maximumStock,
  Integer reorderPoint,
  Integer reorderQuantity,
  BigDecimal unitCost,
  BigDecimal unitPrice,
  String unit,
  UUID supplierId,
  String supplierProductCode,
  String barcode,
  String location,
  boolean expiryTracking,
  boolean batchTracking,
  String imageUrl,
  String notes,
  UUID gymId  // Optional: if null, item is at org level
) {
}
