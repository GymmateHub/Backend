package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.application.port.FreezePolicyRepository;
import com.gymmate.membership.internal.domain.FreezePolicy;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link FreezePolicyRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class FreezePolicyRepositoryAdapter extends DomainRepositoryAdapter implements FreezePolicyRepository {

    private final FreezePolicyJpaRepository jpaRepository;

    public FreezePolicyRepositoryAdapter(FreezePolicyJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FreezePolicy save(FreezePolicy policy) {
        return save(jpaRepository, policy);
    }

    @Override
    public Optional<FreezePolicy> findById(UUID id) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public Optional<FreezePolicy> findActiveByGymId(UUID gymId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public Optional<FreezePolicy> findDefaultPolicy() {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findDefaultPolicy());
    }

    @Override
    public Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findDefaultPolicyByOrganisation(organisationId));
    }

    @Override
    public void delete(FreezePolicy policy) {
        delete(jpaRepository, policy);
    }

    @Override
    public Optional<FreezePolicy> findByGymIdAndActiveTrue(UUID gymId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findByGymIdAndActiveTrue(gymId));
    }

    @Override
    public Optional<FreezePolicy> findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(UUID organisationId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(organisationId));
    }

    @Override
    public List<FreezePolicy> saveAll(Iterable<FreezePolicy> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<FreezePolicy> findAll() {
        return this.<List<FreezePolicy>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<FreezePolicy> findAllById(Iterable<UUID> ids) {
        return this.<List<FreezePolicy>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<FreezePolicy> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public FreezePolicy saveAndFlush(FreezePolicy entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<FreezePolicy> findAll(Pageable pageable) {
        return this.<Page<FreezePolicy>>fromJpa(jpaRepository.findAll(pageable));
    }
}
