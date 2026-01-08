package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.MovementType;
import com.gymmate.inventory.domain.StockMovement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for stock movement response.
 */
public record StockMovementResponse(
  UUID id,
  UUID inventoryItemId,
  MovementType movementType,
  Integer quantity,
  BigDecimal unitCost,
  BigDecimal totalCost,
  Integer stockBefore,
  Integer stockAfter,
  LocalDateTime movementDate,
  String referenceNumber,
  UUID supplierId,
  UUID customerId,
  UUID fromGymId,
  UUID toGymId,
  String batchNumber,
  String notes,
  String performedBy,
  UUID gymId,
  UUID organisationId,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  public static StockMovementResponse fromEntity(StockMovement movement) {
    return new StockMovementResponse(
      movement.getId(),
      movement.getInventoryItemId(),
      movement.getMovementType(),
      movement.getQuantity(),
      movement.getUnitCost(),
      movement.getTotalCost(),
      movement.getStockBefore(),
      movement.getStockAfter(),
      movement.getMovementDate(),
      movement.getReferenceNumber(),
      movement.getSupplierId(),
      movement.getCustomerId(),
      movement.getFromGymId(),
      movement.getToGymId(),
      movement.getBatchNumber(),
      movement.getNotes(),
      movement.getPerformedBy(),
      movement.getGymId(),
      movement.getOrganisationId(),
      movement.getCreatedAt(),
      movement.getUpdatedAt()
    );
  }
}
