package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.StripeWebhookEvent;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.StripeWebhookEventRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link StripeWebhookEventRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the StripeWebhookEvent finders live here.
 */
@Component()
@Transactional()
public class StripeWebhookEventRepositoryAdapter extends JpaDomainRepositoryAdapter<StripeWebhookEvent, UUID, StripeWebhookEventJpaRepository>
        implements StripeWebhookEventRepository {

    public StripeWebhookEventRepositoryAdapter(StripeWebhookEventJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId) {
        return this.<Optional<StripeWebhookEvent>>fromJpa(jpaRepository.findByStripeEventId(stripeEventId));
    }

    @Override
    public boolean existsByStripeEventId(String stripeEventId) {
        return jpaRepository.existsByStripeEventId(stripeEventId);
    }

    @Override
    public boolean existsByStripeEventIdAndProcessedTrue(String stripeEventId) {
        return jpaRepository.existsByStripeEventIdAndProcessedTrue(stripeEventId);
    }
}
