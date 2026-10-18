package com.gymmate.organisation.api.dto;

import java.util.UUID;

/** Billing-relevant view of an organisation. */
public record OrganisationBillingInfo(UUID id, String name, String contactEmail, String billingEmail, UUID ownerUserId) {

    public String preferredEmail() {
        return billingEmail != null && !billingEmail.isBlank() ? billingEmail : contactEmail;
    }
}
