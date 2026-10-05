package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.AccessLog;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.AccessLogRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessLogRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AccessLogRepositoryAdapter extends DomainRepositoryAdapter implements AccessLogRepository {

    private final AccessLogJpaRepository jpaRepository;

    public AccessLogRepositoryAdapter(AccessLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AccessLog> findTopByMemberIdOrderByAccessTimeDesc(UUID memberId) {
        return this.<Optional<AccessLog>>fromJpa(jpaRepository.findTopByMemberIdOrderByAccessTimeDesc(memberId));
    }

    @Override
    public AccessLog save(AccessLog entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AccessLog> saveAll(Iterable<AccessLog> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AccessLog> findById(UUID id) {
        return this.<Optional<AccessLog>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AccessLog> findAll() {
        return this.<List<AccessLog>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AccessLog> findAllById(Iterable<UUID> ids) {
        return this.<List<AccessLog>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(AccessLog entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AccessLog> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AccessLog saveAndFlush(AccessLog entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AccessLog> findAll(Pageable pageable) {
        return this.<Page<AccessLog>>fromJpa(jpaRepository.findAll(pageable));
    }
}
