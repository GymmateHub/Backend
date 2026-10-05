package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.SubscriptionUsage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.SubscriptionUsageRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionUsageRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SubscriptionUsageRepositoryAdapter extends DomainRepositoryAdapter implements SubscriptionUsageRepository {

    private final SubscriptionUsageJpaRepository jpaRepository;

    public SubscriptionUsageRepositoryAdapter(SubscriptionUsageJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SubscriptionUsage> findBySubscriptionAndPeriod(UUID subscriptionId, LocalDateTime date) {
        return this.<Optional<SubscriptionUsage>>fromJpa(jpaRepository.findBySubscriptionAndPeriod(subscriptionId, date));
    }

    @Override
    public List<SubscriptionUsage> findBySubscriptionId(UUID subscriptionId) {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findBySubscriptionId(subscriptionId));
    }

    @Override
    public List<SubscriptionUsage> findByOrganisationId(UUID organisationId) {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<SubscriptionUsage> findUnbilledUsage(LocalDateTime now) {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findUnbilledUsage(now));
    }

    @Override
    public List<SubscriptionUsage> findUsageForBillingPeriod(LocalDateTime start, LocalDateTime end) {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findUsageForBillingPeriod(start, end));
    }

    @Override
    public SubscriptionUsage save(SubscriptionUsage entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<SubscriptionUsage> saveAll(Iterable<SubscriptionUsage> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<SubscriptionUsage> findById(UUID id) {
        return this.<Optional<SubscriptionUsage>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<SubscriptionUsage> findAll() {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<SubscriptionUsage> findAllById(Iterable<UUID> ids) {
        return this.<List<SubscriptionUsage>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(SubscriptionUsage entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<SubscriptionUsage> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public SubscriptionUsage saveAndFlush(SubscriptionUsage entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<SubscriptionUsage> findAll(Pageable pageable) {
        return this.<Page<SubscriptionUsage>>fromJpa(jpaRepository.findAll(pageable));
    }
}
