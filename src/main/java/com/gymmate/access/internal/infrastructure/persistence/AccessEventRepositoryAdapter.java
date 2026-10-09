package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.AccessEvent;
import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.access.internal.application.port.AccessEventRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessEventRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the AccessEvent finders live here.
 */
@Component()
@Transactional()
public class AccessEventRepositoryAdapter extends JpaDomainRepositoryAdapter<AccessEvent, UUID, AccessEventJpaRepository>
        implements AccessEventRepository {

    public AccessEventRepositoryAdapter(AccessEventJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<AccessEvent> findByGymIdOrderByOccurredAtDesc(UUID gymId) {
        return this.<List<AccessEvent>>fromJpa(jpaRepository.findByGymIdOrderByOccurredAtDesc(gymId));
    }

    @Override
    public List<AccessEvent> findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(UUID gymId) {
        return this.<List<AccessEvent>>fromJpa(jpaRepository.findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(gymId));
    }

    @Override
    public Optional<AccessEvent> findTopByMemberIdAndDecisionOrderByOccurredAtDesc(UUID memberId, AccessDecision decision) {
        return this.<Optional<AccessEvent>>fromJpa(jpaRepository.findTopByMemberIdAndDecisionOrderByOccurredAtDesc(memberId, decision));
    }

    @Override
    public Optional<AccessEvent> findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(UUID credentialId, AccessDecision decision, AccessDirection direction) {
        return this.<Optional<AccessEvent>>fromJpa(jpaRepository.findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(credentialId, decision, direction));
    }
}
