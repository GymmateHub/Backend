package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.organisation.internal.application.port.GymRepository;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.constants.GymStatus;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class GymRepositoryAdapter extends DomainRepositoryAdapter implements GymRepository {

    private final GymJpaRepository jpaRepository;

    public GymRepositoryAdapter(GymJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Gym save(Gym gym) {
        return save(jpaRepository, gym);
    }

    @Override
    public Optional<Gym> findById(UUID id) {
        return this.<Optional<Gym>>fromJpa(jpaRepository.findById(id));
    }

    // ========== Organisation-based queries ==========
    @Override
    public List<Gym> findByOrganisationId(UUID organisationId) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Gym> findByOrganisationIdAndStatus(UUID organisationId, GymStatus status) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public Optional<Gym> findBySlug(String slug) {
        return this.<Optional<Gym>>fromJpa(jpaRepository.findBySlug(slug));
    }

    // ========== General queries ==========
    @Override
    public List<Gym> findByStatus(GymStatus status) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByStatus(status));
    }

    @Override
    public List<Gym> findByAddressCity(String city) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByCity(city));
    }

    @Override
    public List<Gym> findAll() {
        return this.<List<Gym>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, GymStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public Integer sumMaxMembersByOrganisationId(UUID organisationId) {
        return jpaRepository.sumMaxMembersByOrganisationId(organisationId);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    @Override
    public List<Gym> findByCity(String city) {
        return this.<List<Gym>>fromJpa(jpaRepository.findByCity(city));
    }

    @Override
    public List<Gym> saveAll(Iterable<Gym> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public List<Gym> findAllById(Iterable<UUID> ids) {
        return this.<List<Gym>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public void delete(Gym entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Gym> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Gym saveAndFlush(Gym entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Gym> findAll(Pageable pageable) {
        return this.<Page<Gym>>fromJpa(jpaRepository.findAll(pageable));
    }
}
