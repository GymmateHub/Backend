package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.MovementType;
import com.gymmate.retail.internal.domain.StockMovement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<StockMovement> saveAll(Iterable<StockMovement> entities);
  
  boolean existsById(UUID id);
  
  List<StockMovement> findAll();
  
  List<StockMovement> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<StockMovement> entities);
  
  StockMovement saveAndFlush(StockMovement entity);
  
  void flush();
  
  Page<StockMovement> findAll(Pageable pageable);
}
