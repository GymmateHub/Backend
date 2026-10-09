package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.ApiRateLimit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.ApiRateLimitRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ApiRateLimitRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the ApiRateLimit finders live here.
 */
@Component()
@Transactional()
public class ApiRateLimitRepositoryAdapter extends JpaDomainRepositoryAdapter<ApiRateLimit, UUID, ApiRateLimitJpaRepository>
        implements ApiRateLimitRepository {

    public ApiRateLimitRepositoryAdapter(ApiRateLimitJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
