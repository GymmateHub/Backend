package com.gymmate.payment.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * The organisation fields billing/notification code needs (name, contact/billing
 * email, owner) — see the port package Javadoc for why this exists instead of
 * {@code payment}/{@code subscription} reading {@code organisation.infrastructure.OrganisationRepository}
 * directly (that was the back-edge closing an organisation/subscription/payment
 * module cycle: organisation legitimately depends on subscription to create one at
 * signup).
 */
public interface OrganisationBillingInfoProvider {

    Optional<BillingInfo> findBillingInfo(UUID organisationId);

    record BillingInfo(UUID id, String name, String contactEmail, String billingEmail, UUID ownerUserId) {

        /** Billing email if set, otherwise contact email — the fallback nearly every caller wants. */
        public String preferredEmail() {
            return billingEmail != null && !billingEmail.isBlank() ? billingEmail : contactEmail;
        }
    }
}
