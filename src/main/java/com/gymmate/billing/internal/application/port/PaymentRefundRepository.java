package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.PaymentRefund;
import com.gymmate.shared.constants.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRefundRepository extends DomainRepository<PaymentRefund, UUID> {

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
       List<PaymentRefund> findByOrganisationIdAndDateRange(
                     UUID organisationId,
                     LocalDateTime startDate,
                     LocalDateTime endDate);

       /**
        * Calculate total refund amount for an organisation within a date range.
        */
       BigDecimal sumRefundAmountByOrganisationIdAndDateRange(
                     UUID organisationId,
                     LocalDateTime startDate,
                     LocalDateTime endDate);

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
