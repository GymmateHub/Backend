package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository interface for InventoryItem domain entity.
 */
public interface InventoryItemRepository {

  InventoryItem save(InventoryItem inventoryItem);

  Optional<InventoryItem> findById(UUID id);

  List<InventoryItem> findByOrganisationId(UUID organisationId);

  List<InventoryItem> findByGymId(UUID gymId);

  Optional<InventoryItem> findBySku(String sku);

  List<InventoryItem> findActiveByGymId(UUID gymId);

  List<InventoryItem> findActiveByOrganisationId(UUID organisationId);

  List<InventoryItem> findLowStockByGymId(UUID gymId);

  List<InventoryItem> findLowStockByOrganisationId(UUID organisationId);

  List<InventoryItem> findReorderNeededByGymId(UUID gymId);

  List<InventoryItem> findReorderNeededByOrganisationId(UUID organisationId);

  void delete(InventoryItem inventoryItem);

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySku(String sku);
  
  long countByGymIdAndCurrentStockLessThanMinimumStock(UUID gymId);
  
  List<InventoryItem> saveAll(Iterable<InventoryItem> entities);
  
  boolean existsById(UUID id);
  
  List<InventoryItem> findAll();
  
  List<InventoryItem> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<InventoryItem> entities);
  
  InventoryItem saveAndFlush(InventoryItem entity);
  
  void flush();
  
  Page<InventoryItem> findAll(Pageable pageable);
}
