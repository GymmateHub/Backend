package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberInvoiceRepository extends JpaRepository<MemberInvoice, UUID> {

    @Query("SELECT mi FROM MemberInvoice mi JOIN Member m ON mi.memberId = m.userId WHERE mi.memberId = :memberId AND m.gymId = :gymId ORDER BY mi.createdAt DESC")
    List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
}

