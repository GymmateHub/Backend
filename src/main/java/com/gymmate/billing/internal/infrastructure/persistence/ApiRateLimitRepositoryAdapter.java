package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.ApiRateLimit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.ApiRateLimitRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ApiRateLimitRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class ApiRateLimitRepositoryAdapter extends DomainRepositoryAdapter implements ApiRateLimitRepository {

    private final ApiRateLimitJpaRepository jpaRepository;

    public ApiRateLimitRepositoryAdapter(ApiRateLimitJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ApiRateLimit> findByOrganisationIdAndWindowStartAndWindowType(UUID organisationId, LocalDateTime windowStart, String windowType) {
        return this.<Optional<ApiRateLimit>>fromJpa(jpaRepository.findByOrganisationIdAndWindowStartAndWindowType(organisationId, windowStart, windowType));
    }

    @Override
    public Optional<ApiRateLimit> findCurrentWindow(UUID organisationId, LocalDateTime now, String windowType) {
        return this.<Optional<ApiRateLimit>>fromJpa(jpaRepository.findCurrentWindow(organisationId, now, windowType));
    }

    @Override
    public List<ApiRateLimit> findActiveBlocks(UUID organisationId, LocalDateTime now) {
        return this.<List<ApiRateLimit>>fromJpa(jpaRepository.findActiveBlocks(organisationId, now));
    }

    @Override
    public List<ApiRateLimit> findExpiredWindows(LocalDateTime cutoffDate) {
        return this.<List<ApiRateLimit>>fromJpa(jpaRepository.findExpiredWindows(cutoffDate));
    }

    @Override
    public void deleteByWindowEndBefore(LocalDateTime cutoffDate) {
        jpaRepository.deleteByWindowEndBefore(cutoffDate);
    }

    @Override
    public Long countBlocksSince(UUID organisationId, LocalDateTime since) {
        return jpaRepository.countBlocksSince(organisationId, since);
    }

    @Override
    public ApiRateLimit save(ApiRateLimit entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<ApiRateLimit> saveAll(Iterable<ApiRateLimit> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<ApiRateLimit> findById(UUID id) {
        return this.<Optional<ApiRateLimit>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ApiRateLimit> findAll() {
        return this.<List<ApiRateLimit>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ApiRateLimit> findAllById(Iterable<UUID> ids) {
        return this.<List<ApiRateLimit>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(ApiRateLimit entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<ApiRateLimit> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ApiRateLimit saveAndFlush(ApiRateLimit entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ApiRateLimit> findAll(Pageable pageable) {
        return this.<Page<ApiRateLimit>>fromJpa(jpaRepository.findAll(pageable));
    }
}
