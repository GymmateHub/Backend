package com.gymmate.organisation.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/** Read model of an organisation for platform-level reporting. */
public record OrganisationInfo(
        UUID id,
        String name,
        String slug,
        String contactEmail,
        UUID ownerUserId,
        String subscriptionPlan,
        String subscriptionStatus,
        boolean active,
        LocalDateTime createdAt) {
}
