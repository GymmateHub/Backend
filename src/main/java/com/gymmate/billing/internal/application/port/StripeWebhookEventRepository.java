package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.StripeWebhookEvent;

import java.util.Optional;
import java.util.UUID;

public interface StripeWebhookEventRepository extends DomainRepository<StripeWebhookEvent, UUID> {

    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);

    boolean existsByStripeEventId(String stripeEventId);

    /**
     * Used for idempotency instead of {@link #existsByStripeEventId}: a row that
     * exists but has {@code processed=false} (a prior attempt failed) should NOT be
     * treated as "already handled" — it must be retried on redelivery, not skipped.
     */
    boolean existsByStripeEventIdAndProcessedTrue(String stripeEventId);
}

