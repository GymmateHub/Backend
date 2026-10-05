package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.organisation.internal.domain.Organisation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.organisation.internal.application.port.OrganisationRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link OrganisationRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class OrganisationRepositoryAdapter extends DomainRepositoryAdapter implements OrganisationRepository {

    private final OrganisationJpaRepository jpaRepository;

    public OrganisationRepositoryAdapter(OrganisationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Organisation> findBySlug(String slug) {
        return this.<Optional<Organisation>>fromJpa(jpaRepository.findBySlug(slug));
    }

    @Override
    public Optional<Organisation> findByOwnerUserId(UUID ownerUserId) {
        return this.<Optional<Organisation>>fromJpa(jpaRepository.findByOwnerUserId(ownerUserId));
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public List<Organisation> findAllActive() {
        return this.<List<Organisation>>fromJpa(jpaRepository.findAllActive());
    }

    @Override
    public List<Organisation> findBySubscriptionStatus(String status) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findBySubscriptionStatus(status));
    }

    @Override
    public List<Organisation> findTrialsEndingBefore(LocalDateTime date) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findTrialsEndingBefore(date));
    }

    @Override
    public List<Organisation> findSubscriptionsExpiringBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findSubscriptionsExpiringBetween(start, end));
    }

    @Override
    public long countActive() {
        return jpaRepository.countActive();
    }

    @Override
    public long countBySubscriptionStatus(String status) {
        return jpaRepository.countBySubscriptionStatus(status);
    }

    @Override
    public List<Organisation> findTrialsEndingBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findTrialsEndingBetween(start, end));
    }

    @Override
    public Organisation save(Organisation entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Organisation> saveAll(Iterable<Organisation> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Organisation> findById(UUID id) {
        return this.<Optional<Organisation>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Organisation> findAll() {
        return this.<List<Organisation>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Organisation> findAllById(Iterable<UUID> ids) {
        return this.<List<Organisation>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(Organisation entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Organisation> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Organisation saveAndFlush(Organisation entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Organisation> findAll(Pageable pageable) {
        return this.<Page<Organisation>>fromJpa(jpaRepository.findAll(pageable));
    }
}
