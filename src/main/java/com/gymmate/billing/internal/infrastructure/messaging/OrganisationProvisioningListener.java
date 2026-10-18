package com.gymmate.billing.internal.infrastructure.messaging;

import com.gymmate.billing.internal.application.SubscriptionService;
import com.gymmate.organisation.api.event.OrganisationCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Provisions the default Starter trial subscription for every new organisation hub.
 *
 * <p>Deliberately a plain, synchronous {@code @EventListener} (not an
 * {@code @ApplicationModuleListener}): it runs inside the publishing transaction, exactly
 * like the direct call it replaces, so a failure rolls the whole hub creation back
 * (same rationale as ADR 0001 §5).
 */
@Component
@RequiredArgsConstructor
class OrganisationProvisioningListener {

    private final SubscriptionService subscriptionService;

    @EventListener
    void on(OrganisationCreatedEvent event) {
        subscriptionService.createSubscription(event.organisationId(), "starter", true);
    }
}
