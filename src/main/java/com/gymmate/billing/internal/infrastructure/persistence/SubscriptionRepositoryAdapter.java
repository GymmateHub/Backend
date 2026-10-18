package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.shared.constants.SubscriptionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.SubscriptionRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Subscription finders live here.
 */
@Component()
@Transactional()
public class SubscriptionRepositoryAdapter extends JpaDomainRepositoryAdapter<Subscription, UUID, SubscriptionJpaRepository>
        implements SubscriptionRepository {

    public SubscriptionRepositoryAdapter(SubscriptionJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
