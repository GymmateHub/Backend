package com.gymmate.organisation.internal.application.dto;

import com.gymmate.organisation.internal.application.OrganisationLimitService.OrganisationUsage;

/**
 * Response DTO for organisation usage statistics.
 * Shows current usage vs limits for gyms, members, and staff.
 */
public record OrganisationUsageResponse(
        // Gym usage
        long currentGyms,
        int maxGyms,
        double gymUsagePercent,
        boolean canAddGym,
        // Member usage
        long currentMembers,
        int maxMembers,
        double memberUsagePercent,
        boolean canAddMember,
        // Staff usage
        long currentStaff,
        int maxStaff,
        double staffUsagePercent,
        boolean canAddStaff,
        // Overall status
        boolean hasUsageWarning,
        boolean hasLimitReached
) {

    public static OrganisationUsageResponse fromUsage(OrganisationUsage usage) {
        return new OrganisationUsageResponse(
                usage.currentGyms(),
                usage.maxGyms(),
                usage.gymUsagePercent(),
                usage.canAddGym(),
                usage.currentMembers(),
                usage.maxMembers(),
                usage.memberUsagePercent(),
                usage.canAddMember(),
                usage.currentStaff(),
                usage.maxStaff(),
                usage.staffUsagePercent(),
                usage.canAddStaff(),
                usage.hasUsageWarning(),
                usage.hasLimitReached());
    }
}
