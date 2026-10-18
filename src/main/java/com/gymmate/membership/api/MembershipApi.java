package com.gymmate.membership.api;

import com.gymmate.membership.api.dto.ActiveMembership;
import com.gymmate.membership.api.dto.PlanMemberCount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Public facade of the membership module. */
public interface MembershipApi {

    // ---- entitlement ----

    Optional<ActiveMembership> findActiveMembership(UUID memberId);

    /**
     * Deducts one class credit from the member's active membership when it has credits left.
     * Joins the caller's transaction.
     *
     * @return true when a credit was deducted
     */
    boolean consumeClassCredit(UUID memberId);

    // ---- reporting ----

    long countActiveMemberships(UUID gymId);

    long countPausedMemberships(UUID gymId);

    long countCancelledMemberships(UUID gymId, LocalDateTime from, LocalDateTime to);

    long countExpiringMemberships(UUID gymId, LocalDateTime from, LocalDateTime to);

    BigDecimal sumProjectedRevenue(UUID gymId, LocalDateTime from, LocalDateTime to);

    List<PlanMemberCount> countActiveMembersByPlan(UUID gymId);

    long countOverdueInvoices(UUID gymId, LocalDateTime asOf);
}
