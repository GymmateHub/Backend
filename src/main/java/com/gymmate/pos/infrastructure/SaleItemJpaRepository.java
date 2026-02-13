package com.gymmate.pos.infrastructure;

import com.gymmate.pos.domain.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for SaleItem entity.
 */
@Repository
public interface SaleItemJpaRepository extends JpaRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleId(UUID saleId);

    List<SaleItem> findByInventoryItemId(UUID inventoryItemId);

    @Query("SELECT si FROM SaleItem si JOIN si.sale s WHERE s.gymId = :gymId AND s.saleDate BETWEEN :startDate AND :endDate")
    List<SaleItem> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT si.inventoryItemId, si.itemName, SUM(si.quantity) as qty, SUM(si.lineTotal) as revenue FROM SaleItem si "
            +
            "JOIN si.sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate "
            +
            "GROUP BY si.inventoryItemId, si.itemName ORDER BY qty DESC")
    List<Object[]> findTopSellingItems(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(si.quantity), 0) FROM SaleItem si JOIN si.sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED'")
    long countItemsSoldByGymId(@Param("gymId") UUID gymId);
}
