package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.SubscriptionUsage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.SubscriptionUsageRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionUsageRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the SubscriptionUsage finders live here.
 */
@Component()
@Transactional()
public class SubscriptionUsageRepositoryAdapter extends JpaDomainRepositoryAdapter<SubscriptionUsage, UUID, SubscriptionUsageJpaRepository>
        implements SubscriptionUsageRepository {

    public SubscriptionUsageRepositoryAdapter(SubscriptionUsageJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
