package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.StripeWebhookEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface StripeWebhookEventRepository {

    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);

    boolean existsByStripeEventId(String stripeEventId);

    /**
     * Used for idempotency instead of {@link #existsByStripeEventId}: a row that
     * exists but has {@code processed=false} (a prior attempt failed) should NOT be
     * treated as "already handled" — it must be retried on redelivery, not skipped.
     */
    boolean existsByStripeEventIdAndProcessedTrue(String stripeEventId);
    
    StripeWebhookEvent save(StripeWebhookEvent entity);
    
    List<StripeWebhookEvent> saveAll(Iterable<StripeWebhookEvent> entities);
    
    Optional<StripeWebhookEvent> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<StripeWebhookEvent> findAll();
    
    List<StripeWebhookEvent> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(StripeWebhookEvent entity);
    
    void deleteAll(Iterable<StripeWebhookEvent> entities);
    
    StripeWebhookEvent saveAndFlush(StripeWebhookEvent entity);
    
    void flush();
    
    Page<StripeWebhookEvent> findAll(Pageable pageable);
}

