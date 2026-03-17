package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.GymInvoice;
import com.gymmate.shared.constants.InvoiceStatus;
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
public interface GymInvoiceRepository extends JpaRepository<GymInvoice, UUID> {

    // ============================================
    // Organisation-based queries (preferred)
    // ============================================

    List<GymInvoice> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    List<GymInvoice> findByOrganisationIdAndStatus(UUID organisationId, InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(gi.amount), 0) FROM GymInvoice gi WHERE gi.organisationId = :orgId AND gi.status = 'PAID' AND gi.paidAt BETWEEN :start AND :end")
    BigDecimal sumPaidAmountByOrganisationIdAndPeriod(@Param("orgId") UUID orgId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ============================================
    // Stripe-based queries
    // ============================================

    Optional<GymInvoice> findByStripeInvoiceId(String stripeInvoiceId);

}
