package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.PaymentRefund;
import com.gymmate.payment.domain.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

    /**
     * Find a refund by its Stripe refund ID.
     */
    Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

    /**
     * Find all refunds for a gym, ordered by creation date descending.
     */
    List<PaymentRefund> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    /**
     * Find all refunds for a specific payment intent.
     */
    List<PaymentRefund> findByStripePaymentIntentIdOrderByCreatedAtDesc(String stripePaymentIntentId);

    /**
     * Find all refunds for a gym with a specific status.
     */
    List<PaymentRefund> findByGymIdAndStatusOrderByCreatedAtDesc(UUID gymId, RefundStatus status);

    /**
     * Find all refunds created within a date range for analytics.
     */
    @Query("SELECT r FROM PaymentRefund r WHERE r.gymId = :gymId " +
           "AND r.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY r.createdAt DESC")
    List<PaymentRefund> findByGymIdAndDateRange(
            @Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Count refunds by status for a gym.
     */
    long countByGymIdAndStatus(UUID gymId, RefundStatus status);

    /**
     * Calculate total refund amount for a gym within a date range.
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r " +
           "WHERE r.gymId = :gymId " +
           "AND r.status = 'SUCCEEDED' " +
           "AND r.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumRefundAmountByGymIdAndDateRange(
            @Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

