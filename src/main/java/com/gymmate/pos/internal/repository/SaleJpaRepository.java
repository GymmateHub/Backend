package com.gymmate.pos.internal.repository;

import com.gymmate.pos.internal.domain.Sale;
import com.gymmate.pos.internal.domain.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Sale entity.
 */
@Repository
public interface SaleJpaRepository extends JpaRepository<Sale, UUID> {

    Optional<Sale> findBySaleNumber(String saleNumber);

    List<Sale> findByGymId(UUID gymId);

    List<Sale> findByGymIdAndStatus(UUID gymId, SaleStatus status);

    List<Sale> findByMemberId(UUID memberId);

    List<Sale> findByStaffId(UUID staffId);

    @Query("SELECT s FROM Sale s WHERE s.gymId = :gymId AND s.saleDate BETWEEN :startDate AND :endDate")
    List<Sale> findByGymIdAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Same filter as {@link #findByGymIdAndDateRange}, as a COUNT query — added so
     * callers that only need a transaction count (e.g. analytics) don't have to fetch
     * every {@code Sale} row just to call {@code .size()} on the result.
     */
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.gymId = :gymId AND s.saleDate BETWEEN :startDate AND :endDate")
    long countByGymIdAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM Sale s WHERE s.gymId = :gymId AND s.saleDate >= :today ORDER BY s.saleDate DESC")
    List<Sale> findTodaysSalesByGymId(@Param("gymId") UUID gymId, @Param("today") LocalDateTime today);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED'")
    long countCompletedByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED'")
    BigDecimal sumTotalByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByGymIdAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM pos_sales WHERE gym_id = :gymId AND status = 'COMPLETED' AND DATE(sale_date) = CURRENT_DATE", nativeQuery = true)
    BigDecimal sumTodaysTotalByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT s.paymentType, COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.gymId = :gymId AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate GROUP BY s.paymentType")
    List<Object[]> sumTotalByPaymentTypeAndDateRange(@Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
