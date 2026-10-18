package com.gymmate.reporting.internal.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganisationSummary(
        UUID id,
        String name,
        String slug,
        String contactEmail,
        String subscriptionPlan,
        String subscriptionStatus,
        long gymCount,
        LocalDateTime createdAt
) {
}
