package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.shared.constants.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface GymInvoiceRepository {

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
    
    GymInvoice save(GymInvoice entity);
    
    List<GymInvoice> saveAll(Iterable<GymInvoice> entities);
    
    Optional<GymInvoice> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<GymInvoice> findAll();
    
    List<GymInvoice> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(GymInvoice entity);
    
    void deleteAll(Iterable<GymInvoice> entities);
    
    GymInvoice saveAndFlush(GymInvoice entity);
    
    void flush();
    
    Page<GymInvoice> findAll(Pageable pageable);

}
