package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.membership.internal.application.port.MemberMembershipRepository;
import com.gymmate.membership.internal.domain.MemberMembership;
import com.gymmate.membership.internal.domain.MembershipStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberMembershipRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MemberMembership finders live here.
 */
@Component
@Transactional()
public class MemberMembershipRepositoryAdapter extends JpaDomainRepositoryAdapter<MemberMembership, UUID, MemberMembershipJpaRepository>
        implements MemberMembershipRepository {

    public MemberMembershipRepositoryAdapter(MemberMembershipJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<MemberMembership> findByMemberId(UUID memberId) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findByMemberId(memberId));
    }

    @Override
    public Optional<MemberMembership> findActiveMembershipByMemberId(UUID memberId) {
        return this.<Optional<MemberMembership>>fromJpa(jpaRepository.findActiveMembershipByMemberId(memberId, LocalDateTime.now()));
    }

    @Override
    public List<MemberMembership> findByGymId(UUID gymId) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<MemberMembership> findByGymIdAndStatus(UUID gymId, MembershipStatus status) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<MemberMembership> findByMemberIdAndGymIdAndStatusIn(UUID memberId, UUID gymId, List<MembershipStatus> statuses) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findByMemberIdAndGymIdAndStatusIn(memberId, gymId, statuses));
    }

    @Override
    public Optional<MemberMembership> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return this.<Optional<MemberMembership>>fromJpa(jpaRepository.findByStripeSubscriptionId(stripeSubscriptionId));
    }

    @Override
    public List<MemberMembership> findExpiringMemberships(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findExpiringMemberships(gymId, startDate, endDate));
    }

    @Override
    public List<MemberMembership> findByPlanId(UUID planId) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findByPlanId(planId));
    }

    @Override
    public long countActiveByGymId(UUID gymId) {
        return jpaRepository.countActiveByGymId(gymId);
    }

    @Override
    public long countByPlanId(UUID planId) {
        return jpaRepository.countByPlanId(planId);
    }

    @Override
    public List<MemberMembership> findFrozenMembershipsToUnfreeze(java.time.LocalDate date) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findFrozenMembershipsToUnfreeze(date));
    }

    @Override
    public List<MemberMembership> findExpiredActiveMemberships(LocalDateTime today) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findExpiredActiveMemberships(today));
    }

    @Override
    public List<MemberMembership> findAutoRenewExpiredMemberships(LocalDateTime today) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findAutoRenewExpiredMemberships(today));
    }

    @Override
    public List<MemberMembership> findStalePastDueMemberships(LocalDateTime cutoff) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findStalePastDueMemberships(cutoff));
    }

    @Override
    public Optional<MemberMembership> findActiveMembershipByMemberId(UUID memberId, LocalDateTime now) {
        return this.<Optional<MemberMembership>>fromJpa(jpaRepository.findActiveMembershipByMemberId(memberId, now));
    }

    @Override
    public long countByGymIdAndStatus(UUID gymId, MembershipStatus status) {
        return jpaRepository.countByGymIdAndStatus(gymId, status);
    }

    @Override
    public long countCancelledByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countCancelledByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public List<Object[]> countActiveMembersByPlan(UUID gymId) {
        return jpaRepository.countActiveMembersByPlan(gymId);
    }

    @Override
    public BigDecimal sumProjectedRevenueByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.sumProjectedRevenueByGymIdAndDateRange(gymId, startDate, endDate);
    }
}
