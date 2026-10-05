package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.AccessPoint;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.AccessPointRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessPointRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AccessPointRepositoryAdapter extends DomainRepositoryAdapter implements AccessPointRepository {

    private final AccessPointJpaRepository jpaRepository;

    public AccessPointRepositoryAdapter(AccessPointJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<AccessPoint> findByGymId(UUID gymId) {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<AccessPoint> findByOrganisationId(UUID organisationId) {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public AccessPoint save(AccessPoint entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AccessPoint> saveAll(Iterable<AccessPoint> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AccessPoint> findById(UUID id) {
        return this.<Optional<AccessPoint>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AccessPoint> findAll() {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AccessPoint> findAllById(Iterable<UUID> ids) {
        return this.<List<AccessPoint>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(AccessPoint entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AccessPoint> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AccessPoint saveAndFlush(AccessPoint entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AccessPoint> findAll(Pageable pageable) {
        return this.<Page<AccessPoint>>fromJpa(jpaRepository.findAll(pageable));
    }
}
