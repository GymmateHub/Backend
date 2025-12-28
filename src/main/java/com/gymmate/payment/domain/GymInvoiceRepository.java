package com.gymmate.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymInvoiceRepository extends JpaRepository<GymInvoice, UUID> {

    List<GymInvoice> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    Optional<GymInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<GymInvoice> findByGymIdAndStatus(UUID gymId, InvoiceStatus status);
}

