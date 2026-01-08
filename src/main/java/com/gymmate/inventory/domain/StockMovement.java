package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockMovement entity representing inventory stock movements/transactions.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "stock_movements")
public class StockMovement extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(name = "inventory_item_id", nullable = false)
  private UUID inventoryItemId;

  @Enumerated(EnumType.STRING)
  @Column(name = "movement_type", nullable = false, length = 50)
  private MovementType movementType;

  @Column(nullable = false)
  private Integer quantity;

  @Column(name = "unit_cost", precision = 10, scale = 2)
  private BigDecimal unitCost;

  @Column(name = "total_cost", precision = 10, scale = 2)
  private BigDecimal totalCost;

  @Column(name = "stock_before", nullable = false)
  private Integer stockBefore;

  @Column(name = "stock_after", nullable = false)
  private Integer stockAfter;

  @Column(name = "movement_date", nullable = false)
  @Builder.Default
  private LocalDateTime movementDate = LocalDateTime.now();

  @Column(name = "reference_number", length = 100)
  private String referenceNumber; // Invoice, PO number, etc.

  @Column(name = "supplier_id")
  private UUID supplierId; // For purchases

  @Column(name = "customer_id")
  private UUID customerId; // For sales (member)

  @Column(name = "from_gym_id")
  private UUID fromGymId; // For transfers

  @Column(name = "to_gym_id")
  private UUID toGymId; // For transfers

  @Column(name = "batch_number", length = 100)
  private String batchNumber;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "performed_by")
  private String performedBy; // User who made the movement

  // Business methods
  public BigDecimal calculateTotalCost() {
    if (unitCost != null && quantity != null) {
      return unitCost.multiply(BigDecimal.valueOf(quantity));
    }
    return totalCost != null ? totalCost : BigDecimal.ZERO;
  }

  public boolean isInbound() {
    return movementType == MovementType.PURCHASE 
        || movementType == MovementType.RETURN 
        || movementType == MovementType.TRANSFER_IN
        || movementType == MovementType.ADJUSTMENT && stockAfter > stockBefore;
  }

  public boolean isOutbound() {
    return movementType == MovementType.SALE 
        || movementType == MovementType.DAMAGE 
        || movementType == MovementType.TRANSFER_OUT
        || movementType == MovementType.ADJUSTMENT && stockAfter < stockBefore;
  }
}
