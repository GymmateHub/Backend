package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * InventoryItem entity representing retail/supply inventory items.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 * Items can be tracked at both organisation level (gymId = null) 
 * or gym level (gymId set).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "inventory_items")
public class InventoryItem extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 100, unique = true)
  private String sku; // Stock Keeping Unit

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Builder.Default
  private InventoryCategory category = InventoryCategory.OTHER;

  @Column(columnDefinition = "TEXT")
  private String description;

  // Stock levels
  @Column(name = "current_stock", nullable = false)
  @Builder.Default
  private Integer currentStock = 0;

  @Column(name = "minimum_stock")
  @Builder.Default
  private Integer minimumStock = 0; // Alert threshold

  @Column(name = "maximum_stock")
  private Integer maximumStock; // Maximum capacity

  @Column(name = "reorder_point")
  @Builder.Default
  private Integer reorderPoint = 0; // When to reorder

  @Column(name = "reorder_quantity")
  private Integer reorderQuantity; // How much to reorder

  // Pricing
  @Column(name = "unit_cost", precision = 10, scale = 2)
  private BigDecimal unitCost; // Cost per unit from supplier

  @Column(name = "unit_price", precision = 10, scale = 2)
  private BigDecimal unitPrice; // Selling price per unit

  @Column(length = 20)
  private String unit; // piece, box, kg, liter, etc.

  // Supplier information
  @Column(name = "supplier_id")
  private UUID supplierId;

  @Column(name = "supplier_product_code", length = 100)
  private String supplierProductCode;

  // Tracking
  @Column(name = "barcode", length = 100)
  private String barcode;

  @Column(name = "location", length = 200)
  private String location; // Storage location within gym

  @Column(name = "expiry_tracking")
  @Builder.Default
  private boolean expiryTracking = false; // For perishable items

  @Column(name = "batch_tracking")
  @Builder.Default
  private boolean batchTracking = false;

  // Additional info
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "low_stock_alert_sent")
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
