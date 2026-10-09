package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for InventoryItem domain entity.
 */
public interface InventoryItemRepository extends DomainRepository<InventoryItem, UUID> {

  List<InventoryItem> findByOrganisationId(UUID organisationId);

  List<InventoryItem> findByGymId(UUID gymId);

  Optional<InventoryItem> findBySku(String sku);

  List<InventoryItem> findActiveByGymId(UUID gymId);

  List<InventoryItem> findActiveByOrganisationId(UUID organisationId);

  List<InventoryItem> findLowStockByGymId(UUID gymId);

  List<InventoryItem> findLowStockByOrganisationId(UUID organisationId);

  List<InventoryItem> findReorderNeededByGymId(UUID gymId);

  List<InventoryItem> findReorderNeededByOrganisationId(UUID organisationId);

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySku(String sku);

  long countByGymIdAndCurrentStockLessThanMinimumStock(UUID gymId);
}
