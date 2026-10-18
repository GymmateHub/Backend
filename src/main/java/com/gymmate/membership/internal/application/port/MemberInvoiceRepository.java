package com.gymmate.membership.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.membership.internal.domain.MemberInvoice;
import com.gymmate.membership.internal.domain.MemberInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberInvoiceRepository extends DomainRepository<MemberInvoice, UUID> {

    // ===== Revenue & Analytics Queries =====

    BigDecimal sumPaidAmountByGymIdAndPeriod(UUID gymId, LocalDateTime start, LocalDateTime end);

    long countOverdueByGymId(UUID gymId, LocalDateTime now);

    List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(UUID memberId, UUID gymId);

    List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
}

