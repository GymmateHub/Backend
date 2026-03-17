package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
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
public interface MemberInvoiceRepository extends JpaRepository<MemberInvoice, UUID> {

    // ===== Revenue & Analytics Queries =====

    @Query("SELECT COALESCE(SUM(mi.amount), 0) FROM MemberInvoice mi WHERE mi.gymId = :gymId AND mi.status = 'PAID' AND mi.paidAt BETWEEN :start AND :end")
    BigDecimal sumPaidAmountByGymIdAndPeriod(@Param("gymId") UUID gymId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(mi) FROM MemberInvoice mi WHERE mi.gymId = :gymId AND mi.status IN ('OPEN', 'PAYMENT_FAILED') AND mi.dueDate < :now")
    long countOverdueByGymId(@Param("gymId") UUID gymId, @Param("now") LocalDateTime now);

    @Query("SELECT mi FROM MemberInvoice mi JOIN Member m ON mi.memberId = m.userId WHERE mi.memberId = :memberId AND m.gymId = :gymId ORDER BY mi.createdAt DESC")
    List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
}

