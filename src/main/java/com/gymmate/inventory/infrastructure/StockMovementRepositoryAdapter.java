package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MovementType;
import com.gymmate.inventory.domain.StockMovement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for StockMovement repository.
 */
@Component
@RequiredArgsConstructor
public class StockMovementRepositoryAdapter implements StockMovementRepository {

  private final StockMovementJpaRepository jpaRepository;

  @Override
  public StockMovement save(StockMovement stockMovement) {
    return jpaRepository.save(stockMovement);
  }

  @Override
  public Optional<StockMovement> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<StockMovement> findByInventoryItemId(UUID inventoryItemId) {
    return jpaRepository.findByInventoryItemId(inventoryItemId);
  }

  @Override
  public List<StockMovement> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<StockMovement> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<StockMovement> findByInventoryItemIdOrderByMovementDateDesc(UUID inventoryItemId) {
    return jpaRepository.findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId);
  }

  @Override
  public List<StockMovement> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
    return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }

  @Override
  public List<StockMovement> findByOrganisationIdAndDateRange(UUID organisationId, LocalDateTime startDate, LocalDateTime endDate) {
    return jpaRepository.findByOrganisationIdAndDateRange(organisationId, startDate, endDate);
  }

  @Override
  public List<StockMovement> findByGymIdAndMovementType(UUID gymId, MovementType movementType) {
    return jpaRepository.findByGymIdAndMovementType(gymId, movementType);
  }

  @Override
  public List<StockMovement> findBySupplierId(UUID supplierId) {
    return jpaRepository.findBySupplierId(supplierId);
  }

  @Override
  public void delete(StockMovement stockMovement) {
    jpaRepository.delete(stockMovement);
  }

  @Override
  public long countByInventoryItemId(UUID inventoryItemId) {
    return jpaRepository.countByInventoryItemId(inventoryItemId);
  }
}
