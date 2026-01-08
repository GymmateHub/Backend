package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MovementType;
import com.gymmate.inventory.domain.StockMovement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for StockMovement domain entity.
 */
public interface StockMovementRepository {

  StockMovement save(StockMovement stockMovement);

  Optional<StockMovement> findById(UUID id);

  List<StockMovement> findByInventoryItemId(UUID inventoryItemId);

  List<StockMovement> findByGymId(UUID gymId);

  List<StockMovement> findByOrganisationId(UUID organisationId);

  List<StockMovement> findByInventoryItemIdOrderByMovementDateDesc(UUID inventoryItemId);

  List<StockMovement> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  List<StockMovement> findByOrganisationIdAndDateRange(UUID organisationId, LocalDateTime startDate, LocalDateTime endDate);

  List<StockMovement> findByGymIdAndMovementType(UUID gymId, MovementType movementType);

  List<StockMovement> findBySupplierId(UUID supplierId);

  void delete(StockMovement stockMovement);

  long countByInventoryItemId(UUID inventoryItemId);
}
