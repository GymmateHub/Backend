package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.InventoryCategory;
import com.gymmate.inventory.domain.InventoryItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for inventory item response.
 */
public record InventoryItemResponse(
  UUID id,
  String name,
  String sku,
  InventoryCategory category,
  String description,
  Integer currentStock,
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
  boolean lowStockAlertSent,
  UUID gymId,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt,
  boolean active
) {
  public static InventoryItemResponse fromEntity(InventoryItem item) {
    return new InventoryItemResponse(
      item.getId(),
      item.getName(),
      item.getSku(),
      item.getCategory(),
      item.getDescription(),
      item.getCurrentStock(),
      item.getMinimumStock(),
      item.getMaximumStock(),
      item.getReorderPoint(),
      item.getReorderQuantity(),
      item.getUnitCost(),
      item.getUnitPrice(),
      item.getUnit(),
      item.getSupplierId(),
      item.getSupplierProductCode(),
      item.getBarcode(),
      item.getLocation(),
      item.isExpiryTracking(),
      item.isBatchTracking(),
      item.getImageUrl(),
      item.getNotes(),
      item.isLowStockAlertSent(),
      item.getGymId(),
      item.getOrganisationId(),
      item.getCreatedAt(),
      item.getUpdatedAt(),
      item.isActive()
    );
  }
}
