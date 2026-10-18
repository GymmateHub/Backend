package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.shared.constants.MemberStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.MemberRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Member finders live here.
 */
@Component()
@Transactional()
public class MemberRepositoryAdapter extends JpaDomainRepositoryAdapter<Member, UUID, MemberJpaRepository>
        implements MemberRepository {

    public MemberRepositoryAdapter(MemberJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<Member> findByOrganisationId(UUID organisationId) {
        return this.<List<Member>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, MemberStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public List<Member> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
        return this.<List<Member>>fromJpa(jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId));
    }

    @Override
    public Optional<Member> findByUserId(UUID userId) {
        return this.<Optional<Member>>fromJpa(jpaRepository.findByUserId(userId));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public Optional<Member> findByUserIdAndGymId(UUID userId, UUID gymId) {
        return this.<Optional<Member>>fromJpa(jpaRepository.findByUserIdAndGymId(userId, gymId));
    }

    @Override
    public boolean existsByUserIdAndGymId(UUID userId, UUID gymId) {
        return jpaRepository.existsByUserIdAndGymId(userId, gymId);
    }

    @Override
    public List<Member> findByGymId(UUID gymId) {
        return this.<List<Member>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public long countByGymId(UUID gymId) {
        return jpaRepository.countByGymId(gymId);
    }

    @Override
    public long countByGymIdAndStatus(UUID gymId, MemberStatus status) {
        return jpaRepository.countByGymIdAndStatus(gymId, status);
    }

    @Override
    public Optional<Member> findByMembershipNumber(String membershipNumber) {
        return this.<Optional<Member>>fromJpa(jpaRepository.findByMembershipNumber(membershipNumber));
    }

    @Override
    public boolean existsByMembershipNumber(String membershipNumber) {
        return jpaRepository.existsByMembershipNumber(membershipNumber);
    }

    @Override
    public List<Member> findByOrganisationIdAndStatus(UUID organisationId, MemberStatus status) {
        return this.<List<Member>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public List<Member> findByOrganisationIdAndWaiverSignedFalse(UUID organisationId) {
        return this.<List<Member>>fromJpa(jpaRepository.findByOrganisationIdAndWaiverSignedFalse(organisationId));
    }

    @Override
    public List<Member> findByOrganisationIdAndJoinDateAfter(UUID organisationId, LocalDate date) {
        return this.<List<Member>>fromJpa(jpaRepository.findByOrganisationIdAndJoinDateAfter(organisationId, date));
    }

    @Override
    public List<Member> findByStatus(MemberStatus status) {
        return this.<List<Member>>fromJpa(jpaRepository.findByStatus(status));
    }

    @Override
    public long countByStatus(MemberStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Member> findByJoinDateBetween(LocalDate startDate, LocalDate endDate) {
        return this.<List<Member>>fromJpa(jpaRepository.findByJoinDateBetween(startDate, endDate));
    }

    @Override
    public List<Member> findByJoinDateAfter(LocalDate date) {
        return this.<List<Member>>fromJpa(jpaRepository.findByJoinDateAfter(date));
    }

    @Override
    public List<Member> findByWaiverSigned(boolean signed) {
        return this.<List<Member>>fromJpa(jpaRepository.findByWaiverSigned(signed));
    }

    @Override
    public List<Member> findByWaiverSignedFalse() {
        return this.<List<Member>>fromJpa(jpaRepository.findByWaiverSignedFalse());
    }

    @Override
    public long countByGymIdAndCreatedAtBetween(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByGymIdAndCreatedAtBetween(gymId, startDate, endDate);
    }
}
