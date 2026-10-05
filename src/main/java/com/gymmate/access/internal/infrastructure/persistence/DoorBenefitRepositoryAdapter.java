package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.DoorBenefit;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.DoorBenefitRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link DoorBenefitRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class DoorBenefitRepositoryAdapter extends DomainRepositoryAdapter implements DoorBenefitRepository {

    private final DoorBenefitJpaRepository jpaRepository;

    public DoorBenefitRepositoryAdapter(DoorBenefitJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByAccessPointId(UUID accessPointId) {
        return jpaRepository.existsByAccessPointId(accessPointId);
    }

    @Override
    public boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId) {
        return jpaRepository.existsByAccessPointIdAndMembershipPlanId(accessPointId, membershipPlanId);
    }

    @Override
    public DoorBenefit save(DoorBenefit entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<DoorBenefit> saveAll(Iterable<DoorBenefit> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<DoorBenefit> findById(UUID id) {
        return this.<Optional<DoorBenefit>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<DoorBenefit> findAll() {
        return this.<List<DoorBenefit>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<DoorBenefit> findAllById(Iterable<UUID> ids) {
        return this.<List<DoorBenefit>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(DoorBenefit entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<DoorBenefit> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public DoorBenefit saveAndFlush(DoorBenefit entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<DoorBenefit> findAll(Pageable pageable) {
        return this.<Page<DoorBenefit>>fromJpa(jpaRepository.findAll(pageable));
    }
}
