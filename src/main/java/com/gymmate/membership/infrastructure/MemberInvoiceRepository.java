package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberInvoiceRepository extends JpaRepository<MemberInvoice, UUID> {

    List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(UUID memberId, UUID gymId);

    List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId);

    Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId);

    List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status);
}

