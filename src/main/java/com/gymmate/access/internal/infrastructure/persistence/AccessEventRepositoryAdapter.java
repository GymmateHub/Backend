package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.AccessEvent;
import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.access.internal.application.port.AccessEventRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link AccessEventRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class AccessEventRepositoryAdapter extends DomainRepositoryAdapter implements AccessEventRepository {

    private final AccessEventJpaRepository jpaRepository;

    public AccessEventRepositoryAdapter(AccessEventJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public AccessEvent save(AccessEvent entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<AccessEvent> saveAll(Iterable<AccessEvent> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<AccessEvent> findById(UUID id) {
        return this.<Optional<AccessEvent>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<AccessEvent> findAll() {
        return this.<List<AccessEvent>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<AccessEvent> findAllById(Iterable<UUID> ids) {
        return this.<List<AccessEvent>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(AccessEvent entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<AccessEvent> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public AccessEvent saveAndFlush(AccessEvent entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<AccessEvent> findAll(Pageable pageable) {
        return this.<Page<AccessEvent>>fromJpa(jpaRepository.findAll(pageable));
    }
}
