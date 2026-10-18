package com.gymmate.organisation.api.event;

import java.util.UUID;

/**
 * Published inside the creating transaction when a new organisation hub (organisation +
 * owner link) is created. Billing provisions the default trial subscription from it.
 */
public record OrganisationCreatedEvent(UUID organisationId, String name, String contactEmail, UUID ownerUserId) {
}
