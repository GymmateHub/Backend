package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * StockMovement entity representing inventory stock movements/transactions.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class StockMovement extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private UUID inventoryItemId;

  private 
  MovementType movementType;

  private Integer quantity;

  private BigDecimal unitCost;

  private BigDecimal totalCost;

  private Integer stockBefore;

  private Integer stockAfter;

  @Builder.Default
  private 
  LocalDateTime movementDate = LocalDateTime.now();

  private String referenceNumber; // Invoice, PO number, etc.

  private UUID supplierId; // For purchases

  private UUID customerId; // For sales (member)

  private UUID fromGymId; // For transfers

  private UUID toGymId; // For transfers

  private String batchNumber;

  private String notes;

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
