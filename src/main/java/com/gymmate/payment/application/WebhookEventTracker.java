package com.gymmate.payment.application;

import com.gymmate.payment.domain.StripeWebhookEvent;
import com.gymmate.payment.infrastructure.StripeWebhookEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists the idempotency/audit trail for Stripe webhook processing in its own
 * transactions (REQUIRES_NEW), independent of whether the event processing itself
 * commits or rolls back. This is what lets {@link StripeWebhookService} both (a) keep
 * a durable failure record even when it rethrows to signal Stripe for redelivery, and
 * (b) roll back partial event-processing side effects on failure without losing that
 * record — the two need different transaction boundaries.
 */
@Service
@RequiredArgsConstructor
public class WebhookEventTracker {

    private final StripeWebhookEventRepository webhookEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPending(String stripeEventId, String eventType, String payload) {
        StripeWebhookEvent webhookEvent = StripeWebhookEvent.builder()
                .stripeEventId(stripeEventId)
                .eventType(eventType)
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String stripeEventId) {
        webhookEventRepository.findByStripeEventId(stripeEventId).ifPresent(e -> {
            e.markProcessed();
            webhookEventRepository.save(e);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String stripeEventId, String errorMessage) {
        webhookEventRepository.findByStripeEventId(stripeEventId).ifPresent(e -> {
            e.markFailed(errorMessage);
            webhookEventRepository.save(e);
        });
    }
}
