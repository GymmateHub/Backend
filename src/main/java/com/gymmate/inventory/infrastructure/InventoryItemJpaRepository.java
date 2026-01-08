package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for InventoryItem entity.
 */
@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItem, UUID> {

  List<InventoryItem> findByOrganisationId(UUID organisationId);

  List<InventoryItem> findByGymId(UUID gymId);

  Optional<InventoryItem> findBySku(String sku);

  @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.active = true")
  List<InventoryItem> findActiveByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.active = true")
  List<InventoryItem> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.currentStock <= i.minimumStock")
  List<InventoryItem> findLowStockByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.currentStock <= i.minimumStock")
  List<InventoryItem> findLowStockByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.currentStock <= i.reorderPoint")
  List<InventoryItem> findReorderNeededByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.currentStock <= i.reorderPoint")
  List<InventoryItem> findReorderNeededByOrganisationId(@Param("organisationId") UUID organisationId);

  long countByGymId(UUID gymId);

  long countByOrganisationId(UUID organisationId);

  boolean existsBySku(String sku);
}
