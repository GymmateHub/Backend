package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, UUID> {

    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);

    boolean existsByStripeEventId(String stripeEventId);

    /**
     * Used for idempotency instead of {@link #existsByStripeEventId}: a row that
     * exists but has {@code processed=false} (a prior attempt failed) should NOT be
     * treated as "already handled" — it must be retried on redelivery, not skipped.
     */
    boolean existsByStripeEventIdAndProcessedTrue(String stripeEventId);
}

