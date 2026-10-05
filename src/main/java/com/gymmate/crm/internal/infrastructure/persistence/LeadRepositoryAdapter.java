package com.gymmate.crm.internal.infrastructure.persistence;

import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.crm.internal.application.port.LeadRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link LeadRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class LeadRepositoryAdapter extends DomainRepositoryAdapter implements LeadRepository {

    private final LeadJpaRepository jpaRepository;

    public LeadRepositoryAdapter(LeadJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Lead> findByGymId(UUID gymId) {
        return this.<List<Lead>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<Lead> findByOrganisationId(UUID organisationId) {
        return this.<List<Lead>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status) {
        return this.<List<Lead>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<Lead> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status) {
        return this.<List<Lead>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public Lead save(Lead entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Lead> saveAll(Iterable<Lead> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Lead> findById(UUID id) {
        return this.<Optional<Lead>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Lead> findAll() {
        return this.<List<Lead>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Lead> findAllById(Iterable<UUID> ids) {
        return this.<List<Lead>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(Lead entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Lead> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Lead saveAndFlush(Lead entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Lead> findAll(Pageable pageable) {
        return this.<Page<Lead>>fromJpa(jpaRepository.findAll(pageable));
    }
}
