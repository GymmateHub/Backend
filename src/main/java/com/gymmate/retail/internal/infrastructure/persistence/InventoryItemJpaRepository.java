package com.gymmate.retail.internal.infrastructure.persistence;

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
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID> {

    List<InventoryItemJpaEntity> findByOrganisationId(UUID organisationId);

    List<InventoryItemJpaEntity> findByGymId(UUID gymId);

    Optional<InventoryItemJpaEntity> findBySku(String sku);

    @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.active = true")
    List<InventoryItemJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.active = true")
    List<InventoryItemJpaEntity> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.currentStock <= i.minimumStock")
    List<InventoryItemJpaEntity> findLowStockByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.currentStock <= i.minimumStock")
    List<InventoryItemJpaEntity> findLowStockByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("SELECT i FROM InventoryItem i WHERE i.gymId = :gymId AND i.currentStock <= i.reorderPoint")
    List<InventoryItemJpaEntity> findReorderNeededByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT i FROM InventoryItem i WHERE i.organisationId = :organisationId AND i.currentStock <= i.reorderPoint")
    List<InventoryItemJpaEntity> findReorderNeededByOrganisationId(@Param("organisationId") UUID organisationId);

    long countByGymId(UUID gymId);

    long countByOrganisationId(UUID organisationId);

    boolean existsBySku(String sku);

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.gymId = :gymId AND i.currentStock < i.minimumStock")
    long countByGymIdAndCurrentStockLessThanMinimumStock(@Param("gymId") UUID gymId);
}
