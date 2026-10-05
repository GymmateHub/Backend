package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.shared.constants.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.SubscriptionRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SubscriptionRepositoryAdapter extends DomainRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Subscription> findByOrganisationId(UUID organisationId) {
        return this.<Optional<Subscription>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return this.<Optional<Subscription>>fromJpa(jpaRepository.findByStripeSubscriptionId(stripeSubscriptionId));
    }

    @Override
    public Optional<Subscription> findByStripeCustomerId(String stripeCustomerId) {
        return this.<Optional<Subscription>>fromJpa(jpaRepository.findByStripeCustomerId(stripeCustomerId));
    }

    @Override
    public List<Subscription> findByStatus(SubscriptionStatus status) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findByStatus(status));
    }

    @Override
    public List<Subscription> findByStatuses(List<SubscriptionStatus> statuses) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findByStatuses(statuses));
    }

    @Override
    public List<Subscription> findExpiredSubscriptions(LocalDateTime now, SubscriptionStatus status) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findExpiredSubscriptions(now, status));
    }

    @Override
    public List<Subscription> findSubscriptionsExpiringBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findSubscriptionsExpiringBetween(start, end));
    }

    @Override
    public List<Subscription> findTrialsEndingBetween(LocalDateTime start, LocalDateTime end) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findTrialsEndingBetween(start, end));
    }

    @Override
    public List<Subscription> findCancelledSubscriptionsToProcess(LocalDateTime date) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findCancelledSubscriptionsToProcess(date));
    }

    @Override
    public long countByStatus(SubscriptionStatus status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Subscription> findStalePastDueSubscriptions(LocalDateTime cutoff) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findStalePastDueSubscriptions(cutoff));
    }

    @Override
    public boolean existsByOrganisationId(UUID organisationId) {
        return jpaRepository.existsByOrganisationId(organisationId);
    }

    @Override
    public Subscription save(Subscription entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Subscription> saveAll(Iterable<Subscription> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return this.<Optional<Subscription>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Subscription> findAll() {
        return this.<List<Subscription>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Subscription> findAllById(Iterable<UUID> ids) {
        return this.<List<Subscription>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(Subscription entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Subscription> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Subscription saveAndFlush(Subscription entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Subscription> findAll(Pageable pageable) {
        return this.<Page<Subscription>>fromJpa(jpaRepository.findAll(pageable));
    }
}
