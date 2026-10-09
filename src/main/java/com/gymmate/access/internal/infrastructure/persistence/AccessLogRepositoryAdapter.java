package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.AccessLog;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.access.internal.application.port.AccessLogRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessLogRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AccessLog finders live here.
 */
@Component()
@Transactional()
public class AccessLogRepositoryAdapter extends JpaDomainRepositoryAdapter<AccessLog, UUID, AccessLogJpaRepository>
        implements AccessLogRepository {

    public AccessLogRepositoryAdapter(AccessLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<AccessLog> findTopByMemberIdOrderByAccessTimeDesc(UUID memberId) {
        return this.<Optional<AccessLog>>fromJpa(jpaRepository.findTopByMemberIdOrderByAccessTimeDesc(memberId));
    }
}
