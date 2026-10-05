package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.StripeWebhookEvent;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.StripeWebhookEventRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link StripeWebhookEventRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class StripeWebhookEventRepositoryAdapter extends DomainRepositoryAdapter implements StripeWebhookEventRepository {

    private final StripeWebhookEventJpaRepository jpaRepository;

    public StripeWebhookEventRepositoryAdapter(StripeWebhookEventJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public StripeWebhookEvent save(StripeWebhookEvent entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<StripeWebhookEvent> saveAll(Iterable<StripeWebhookEvent> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<StripeWebhookEvent> findById(UUID id) {
        return this.<Optional<StripeWebhookEvent>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<StripeWebhookEvent> findAll() {
        return this.<List<StripeWebhookEvent>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<StripeWebhookEvent> findAllById(Iterable<UUID> ids) {
        return this.<List<StripeWebhookEvent>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(StripeWebhookEvent entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<StripeWebhookEvent> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public StripeWebhookEvent saveAndFlush(StripeWebhookEvent entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<StripeWebhookEvent> findAll(Pageable pageable) {
        return this.<Page<StripeWebhookEvent>>fromJpa(jpaRepository.findAll(pageable));
    }
}
