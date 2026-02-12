package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.PaymentRefund;
import com.gymmate.payment.domain.RefundStatus;
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
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, UUID> {

       // ============================================
       // Organisation-based queries (preferred)
       // ============================================

       /**
        * Find all refunds for an organisation, ordered by creation date descending.
        */
       List<PaymentRefund> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

       /**
        * Find all refunds for an organisation with a specific status.
        */
       List<PaymentRefund> findByOrganisationIdAndStatusOrderByCreatedAtDesc(UUID organisationId, RefundStatus status);

       /**
        * Count refunds by status for an organisation.
        */
       long countByOrganisationIdAndStatus(UUID organisationId, RefundStatus status);

       /**
        * Find all refunds for an organisation created within a date range.
        */
       @Query("SELECT r FROM PaymentRefund r WHERE r.organisationId = :organisationId " +
                     "AND r.createdAt BETWEEN :startDate AND :endDate " +
                     "ORDER BY r.createdAt DESC")
       List<PaymentRefund> findByOrganisationIdAndDateRange(
                     @Param("organisationId") UUID organisationId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       /**
        * Calculate total refund amount for an organisation within a date range.
        */
       @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r " +
                     "WHERE r.organisationId = :organisationId " +
                     "AND r.status = 'SUCCEEDED' " +
                     "AND r.createdAt BETWEEN :startDate AND :endDate")
       BigDecimal sumRefundAmountByOrganisationIdAndDateRange(
                     @Param("organisationId") UUID organisationId,
                     @Param("startDate") LocalDateTime startDate,
                     @Param("endDate") LocalDateTime endDate);

       // ============================================
       // Stripe-based queries
       // ============================================

       /**
        * Find a refund by its Stripe refund ID.
        */
       Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

       /**
        * Find all refunds for a specific payment intent.
        */
       List<PaymentRefund> findByStripePaymentIntentIdOrderByCreatedAtDesc(String stripePaymentIntentId);

       // ============================================
       // Gym-based queries (backward compatible) -> REMOVED
}
