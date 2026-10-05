package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.SubscriptionTier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.SubscriptionTierRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionTierRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SubscriptionTierRepositoryAdapter extends DomainRepositoryAdapter implements SubscriptionTierRepository {

    private final SubscriptionTierJpaRepository jpaRepository;

    public SubscriptionTierRepositoryAdapter(SubscriptionTierJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SubscriptionTier> findByName(String name) {
        return this.<Optional<SubscriptionTier>>fromJpa(jpaRepository.findByName(name));
    }

    @Override
    public List<SubscriptionTier> findByActiveTrueOrderBySortOrder() {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findByActiveTrueOrderBySortOrder());
    }

    @Override
    public List<SubscriptionTier> findFeaturedTiers() {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findFeaturedTiers());
    }

    @Override
    public List<SubscriptionTier> findSuitableTiersForMemberCount(Integer memberCount) {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findSuitableTiersForMemberCount(memberCount));
    }

    @Override
    public SubscriptionTier save(SubscriptionTier entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<SubscriptionTier> saveAll(Iterable<SubscriptionTier> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<SubscriptionTier> findById(UUID id) {
        return this.<Optional<SubscriptionTier>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<SubscriptionTier> findAll() {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<SubscriptionTier> findAllById(Iterable<UUID> ids) {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(SubscriptionTier entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<SubscriptionTier> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public SubscriptionTier saveAndFlush(SubscriptionTier entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<SubscriptionTier> findAll(Pageable pageable) {
        return this.<Page<SubscriptionTier>>fromJpa(jpaRepository.findAll(pageable));
    }
}
