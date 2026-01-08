package com.gymmate.inventory.api.dto;

import com.gymmate.inventory.domain.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for recording stock movement.
 */
public record StockMovementRequest(
  @NotNull(message = "Movement type is required")
  MovementType movementType,

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  Integer quantity,

  BigDecimal unitCost,
  UUID supplierId,
  UUID customerId,
  String referenceNumber,
  String notes,
  String performedBy
) {
}
