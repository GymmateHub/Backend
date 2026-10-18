package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.domain.MemberInvoiceStatus;
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
public interface MemberInvoiceJpaRepository extends JpaRepository<MemberInvoiceJpaEntity, UUID> {

    // ===== Revenue & Analytics Queries =====
    @Query("SELECT COALESCE(SUM(mi.amount), 0) FROM MemberInvoice mi WHERE mi.gymId = :gymId AND mi.status = 'PAID' AND mi.paidAt BETWEEN :start AND :end")
    BigDecimal sumPaidAmountByGymIdAndPeriod(@Param("gymId") UUID gymId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(mi) FROM MemberInvoice mi WHERE mi.gymId = :gymId AND mi.status IN ('OPEN', 'PAYMENT_FAILED') AND mi.dueDate < :now")
    long countOverdueByGymId(@Param("gymId") UUID gymId, @Param("now") LocalDateTime now);

    @Query("SELECT mi FROM MemberInvoice mi WHERE mi.memberId = :memberId AND mi.gymId = :gymId ORDER BY mi.createdAt DESC")
    List<MemberInvoiceJpaEntity> findByMemberIdAndGymIdOrderByCreatedAtDesc(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    List<MemberInvoiceJpaEntity> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoiceJpaEntity> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoiceJpaEntity> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
}
