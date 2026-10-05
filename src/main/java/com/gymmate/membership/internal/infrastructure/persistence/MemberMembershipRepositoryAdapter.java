package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.application.port.MemberMembershipRepository;
import com.gymmate.membership.internal.domain.MemberMembership;
import com.gymmate.membership.internal.domain.MembershipStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberMembershipRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class MemberMembershipRepositoryAdapter extends DomainRepositoryAdapter implements MemberMembershipRepository {

    private final MemberMembershipJpaRepository jpaRepository;

    public MemberMembershipRepositoryAdapter(MemberMembershipJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MemberMembership save(MemberMembership membership) {
        return save(jpaRepository, membership);
    }

    @Override
    public Optional<MemberMembership> findById(UUID id) {
        return this.<Optional<MemberMembership>>fromJpa(jpaRepository.findById(id));
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
    public void delete(MemberMembership membership) {
        delete(jpaRepository, membership);
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

    @Override
    public List<MemberMembership> saveAll(Iterable<MemberMembership> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MemberMembership> findAll() {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MemberMembership> findAllById(Iterable<UUID> ids) {
        return this.<List<MemberMembership>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<MemberMembership> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MemberMembership saveAndFlush(MemberMembership entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MemberMembership> findAll(Pageable pageable) {
        return this.<Page<MemberMembership>>fromJpa(jpaRepository.findAll(pageable));
    }
}
