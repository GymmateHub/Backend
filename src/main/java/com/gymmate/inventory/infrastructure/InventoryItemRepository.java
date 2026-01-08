package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}
