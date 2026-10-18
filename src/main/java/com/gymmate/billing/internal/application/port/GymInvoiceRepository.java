package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.shared.constants.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GymInvoiceRepository extends DomainRepository<GymInvoice, UUID> {

    // ============================================
    // Organisation-based queries (preferred)
    // ============================================

    List<GymInvoice> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId);

    List<GymInvoice> findByOrganisationIdAndStatus(UUID organisationId, InvoiceStatus status);

    BigDecimal sumPaidAmountByOrganisationIdAndPeriod(UUID orgId, LocalDateTime start, LocalDateTime end);

    // ============================================
    // Stripe-based queries
    // ============================================

    Optional<GymInvoice> findByStripeInvoiceId(String stripeInvoiceId);
}
