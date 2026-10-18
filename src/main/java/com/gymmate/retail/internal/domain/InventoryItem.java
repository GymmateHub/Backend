package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * InventoryItem entity representing retail/supply inventory items.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 * Items can be tracked at both organisation level (gymId = null)
 * or gym level (gymId set).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class InventoryItem extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String name;

  private String sku; // Stock Keeping Unit

  @Builder.Default
  private InventoryCategory category = InventoryCategory.OTHER;

  private String description;

  // Stock levels
  @Builder.Default
  private Integer currentStock = 0;

  @Builder.Default
  private Integer minimumStock = 0; // Alert threshold

  private Integer maximumStock; // Maximum capacity

  @Builder.Default
  private Integer reorderPoint = 0; // When to reorder

  private Integer reorderQuantity; // How much to reorder

  // Pricing
  private BigDecimal unitCost; // Cost per unit from supplier

  private BigDecimal unitPrice; // Selling price per unit

  private String unit; // piece, box, kg, liter, etc.

  // Supplier information
  private UUID supplierId;

  private String supplierProductCode;

  // Tracking
  private String barcode;

  private String location; // Storage location within gym

  @Builder.Default
  private boolean expiryTracking = false; // For perishable items

  @Builder.Default
  private boolean batchTracking = false;

  // Additional info
  private String imageUrl;

  private String notes;

  @Builder.Default
  private boolean lowStockAlertSent = false;

  // Business methods
  public void increaseStock(int quantity) {
    this.currentStock = (this.currentStock == null ? 0 : this.currentStock) + quantity;
    if (this.currentStock > this.minimumStock) {
      this.lowStockAlertSent = false;
    }
  }

  public void decreaseStock(int quantity) {
    this.currentStock = (this.currentStock == null ? 0 : this.currentStock) - quantity;
    if (this.currentStock < 0) {
      this.currentStock = 0;
    }
  }

  public void setStock(int quantity) {
    this.currentStock = quantity;
    if (this.currentStock > this.minimumStock) {
      this.lowStockAlertSent = false;
    }
  }

  public boolean isLowStock() {
    return currentStock != null && minimumStock != null && currentStock <= minimumStock;
  }

  public boolean needsReorder() {
    return reorderPoint != null && currentStock != null && currentStock <= reorderPoint;
  }

  public BigDecimal getTotalValue() {
    if (currentStock == null || unitCost == null) {
      return BigDecimal.ZERO;
    }
    return unitCost.multiply(BigDecimal.valueOf(currentStock));
  }

  public void updatePricing(BigDecimal unitCost, BigDecimal unitPrice) {
    this.unitCost = unitCost;
    this.unitPrice = unitPrice;
  }

  public void markLowStockAlertSent() {
    this.lowStockAlertSent = true;
  }
}
