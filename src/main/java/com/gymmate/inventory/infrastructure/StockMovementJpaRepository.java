package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.MovementType;
import com.gymmate.inventory.domain.StockMovement;
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
public interface StockMovementJpaRepository extends JpaRepository<StockMovement, UUID> {

  List<StockMovement> findByInventoryItemId(UUID inventoryItemId);

  List<StockMovement> findByGymId(UUID gymId);

  List<StockMovement> findByOrganisationId(UUID organisationId);

  @Query("SELECT s FROM StockMovement s WHERE s.inventoryItemId = :inventoryItemId ORDER BY s.movementDate DESC")
  List<StockMovement> findByInventoryItemIdOrderByMovementDateDesc(@Param("inventoryItemId") UUID inventoryItemId);

  @Query("SELECT s FROM StockMovement s WHERE s.gymId = :gymId AND s.movementDate BETWEEN :startDate AND :endDate")
  List<StockMovement> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

  @Query("SELECT s FROM StockMovement s WHERE s.organisationId = :organisationId AND s.movementDate BETWEEN :startDate AND :endDate")
  List<StockMovement> findByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

  List<StockMovement> findByGymIdAndMovementType(UUID gymId, MovementType movementType);

  List<StockMovement> findBySupplierId(UUID supplierId);

  long countByInventoryItemId(UUID inventoryItemId);
}
