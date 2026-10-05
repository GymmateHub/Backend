package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface // ============================================
// Gym-based queries (backward compatible) -> REMOVED
PaymentRefundJpaRepository extends JpaRepository<PaymentRefundJpaEntity, UUID> {

    // ============================================
    // Organisation-based queries (preferred)
    // ============================================
    /**
     * Find all refunds for an organisation, ordered by creation date descending.
     */
    List<PaymentRefundJpaEntity> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    /**
     * Find all refunds for an organisation with a specific status.
     */
    List<PaymentRefundJpaEntity> findByOrganisationIdAndStatusOrderByCreatedAtDesc(UUID organisationId, RefundStatus status);

    /**
     * Count refunds by status for an organisation.
     */
    long countByOrganisationIdAndStatus(UUID organisationId, RefundStatus status);

    /**
     * Find all refunds for an organisation created within a date range.
     */
    @Query("SELECT r FROM PaymentRefund r WHERE r.organisationId = :organisationId " + "AND r.createdAt BETWEEN :startDate AND :endDate " + "ORDER BY r.createdAt DESC")
    List<PaymentRefundJpaEntity> findByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total refund amount for an organisation within a date range.
     */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r " + "WHERE r.organisationId = :organisationId " + "AND r.status = 'SUCCEEDED' " + "AND r.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumRefundAmountByOrganisationIdAndDateRange(@Param("organisationId") UUID organisationId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ============================================
    // Stripe-based queries
    // ============================================
    /**
     * Find a refund by its Stripe refund ID.
     */
    Optional<PaymentRefundJpaEntity> findByStripeRefundId(String stripeRefundId);

    /**
     * Find all refunds for a specific payment intent.
     */
    List<PaymentRefundJpaEntity> findByStripePaymentIntentIdOrderByCreatedAtDesc(String stripePaymentIntentId);
}
