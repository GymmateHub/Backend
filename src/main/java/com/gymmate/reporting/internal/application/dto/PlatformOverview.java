package com.gymmate.reporting.internal.application.dto;

import java.util.List;

public record PlatformOverview(
        long totalOrganisations,
        long totalGyms,
        long totalUsers,
        long totalOwners,
        long totalMembers,
        List<OrganisationSummary> recentOrganisations
) {
}
