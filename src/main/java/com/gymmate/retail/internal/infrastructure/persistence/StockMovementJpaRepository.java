package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.domain.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for StockMovement entity.
 */
@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {

    List<StockMovementJpaEntity> findByInventoryItemId(UUID inventoryItemId);

    List<StockMovementJpaEntity> findByGymId(UUID gymId);

    List<StockMovementJpaEntity> findByOrganisationId(UUID organisationId);

    @Query("SELECT s FROM StockMovement s WHERE s.inventoryItemId = :inventoryItemId ORDER BY s.movementDate DESC")
    List<StockMovementJpaEntity> findByInventoryItemIdOrderByMovementDateDesc(@Param("inventoryItemId") UUID inventoryItemId);

    @Query("SELECT s FROM StockMovement s WHERE s.gymId = :gymId AND s.movementDate BETWEEN :startDate AND :endDate")
    List<StockMovementJpaEntity> findByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM StockMovement s WHERE s.organisationId = :organisationId AND s.movementDate BETWEEN :startDate AND :endDate")
    List<StockMovementJpaEntity> findByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    List<StockMovementJpaEntity> findByGymIdAndMovementType(UUID gymId, MovementType movementType);

    List<StockMovementJpaEntity> findBySupplierId(UUID supplierId);

    long countByInventoryItemId(UUID inventoryItemId);
}
