package com.gymmate.membership.internal.application;

import com.gymmate.membership.api.MembershipApi;
import com.gymmate.membership.api.dto.ActiveMembership;
import com.gymmate.membership.api.dto.PlanMemberCount;
import com.gymmate.membership.internal.application.port.MemberInvoiceRepository;
import com.gymmate.membership.internal.application.port.MemberMembershipRepository;
import com.gymmate.membership.internal.domain.MemberMembership;
import com.gymmate.membership.internal.domain.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implementation of the membership module's public facade. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipApiService implements MembershipApi {

    private final MemberMembershipRepository memberships;
    private final MemberInvoiceRepository invoices;

    @Override
    public Optional<ActiveMembership> findActiveMembership(UUID memberId) {
        return memberships.findActiveMembershipByMemberId(memberId).map(MembershipApiService::toActive);
    }

    @Override
    @Transactional
    public boolean consumeClassCredit(UUID memberId) {
        Optional<MemberMembership> active = memberships.findActiveMembershipByMemberId(memberId);
        if (active.isEmpty()) {
            return false;
        }
        MemberMembership membership = active.get();
        Integer credits = membership.getClassCreditsRemaining();
        if (credits == null || credits <= 0) {
            return false;
        }
        membership.setClassCreditsRemaining(credits - 1);
        memberships.save(membership);
        return true;
    }

    @Override
    public long countActiveMemberships(UUID gymId) {
        return memberships.countActiveByGymId(gymId);
    }

    @Override
    public long countPausedMemberships(UUID gymId) {
        return memberships.countByGymIdAndStatus(gymId, MembershipStatus.PAUSED);
    }

    @Override
    public long countCancelledMemberships(UUID gymId, LocalDateTime from, LocalDateTime to) {
        return memberships.countCancelledByGymIdAndDateRange(gymId, from, to);
    }

    @Override
    public long countExpiringMemberships(UUID gymId, LocalDateTime from, LocalDateTime to) {
        return memberships.findExpiringMemberships(gymId, from, to).size();
    }

    @Override
    public BigDecimal sumProjectedRevenue(UUID gymId, LocalDateTime from, LocalDateTime to) {
        return memberships.sumProjectedRevenueByGymIdAndDateRange(gymId, from, to);
    }

    @Override
    public List<PlanMemberCount> countActiveMembersByPlan(UUID gymId) {
        return memberships.countActiveMembersByPlan(gymId).stream()
                .map(row -> new PlanMemberCount((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    @Override
    public long countOverdueInvoices(UUID gymId, LocalDateTime asOf) {
        return invoices.countOverdueByGymId(gymId, asOf);
    }

    private static ActiveMembership toActive(MemberMembership m) {
        return new ActiveMembership(m.getId(), m.getMemberId(), m.getMembershipPlanId(), m.getClassCreditsRemaining());
    }
}
