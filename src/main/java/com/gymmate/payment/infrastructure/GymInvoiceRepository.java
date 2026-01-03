package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.GymInvoice;
import com.gymmate.payment.domain.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    // ============================================
    // Stripe-based queries
    // ============================================

    Optional<GymInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    // ============================================
    // Gym-based queries (backward compatible)
    // ============================================

    @Deprecated(since = "1.0", forRemoval = true)
    List<GymInvoice> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    @Deprecated(since = "1.0", forRemoval = true)
    List<GymInvoice> findByGymIdAndStatus(UUID gymId, InvoiceStatus status);
}

