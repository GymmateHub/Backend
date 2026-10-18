package com.gymmate.reporting.internal.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantSummary(
        UUID id,
        String name,
        String slug,
        String ownerName,
        String contactEmail,
        long gymCount,
        long memberCount,
        String plan,
        String status,
        LocalDateTime createdAt
) {
}
