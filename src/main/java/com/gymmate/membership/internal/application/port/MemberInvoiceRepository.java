package com.gymmate.membership.internal.application.port;

import com.gymmate.membership.internal.domain.MemberInvoice;
import com.gymmate.membership.internal.domain.MemberInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface MemberInvoiceRepository {

    // ===== Revenue & Analytics Queries =====

    BigDecimal sumPaidAmountByGymIdAndPeriod(UUID gymId, LocalDateTime start, LocalDateTime end);

    long countOverdueByGymId(UUID gymId, LocalDateTime now);

    List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(UUID memberId, UUID gymId);

    List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
    
    MemberInvoice save(MemberInvoice entity);
    
    List<MemberInvoice> saveAll(Iterable<MemberInvoice> entities);
    
    Optional<MemberInvoice> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<MemberInvoice> findAll();
    
    List<MemberInvoice> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(MemberInvoice entity);
    
    void deleteAll(Iterable<MemberInvoice> entities);
    
    MemberInvoice saveAndFlush(MemberInvoice entity);
    
    void flush();
    
    Page<MemberInvoice> findAll(Pageable pageable);
}

