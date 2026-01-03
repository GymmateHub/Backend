package com.gymmate.shared.api.dto;

import com.gymmate.shared.service.OrganisationLimitService.OrganisationUsage;
import lombok.Builder;
import lombok.Data;

/**
 * Response DTO for organisation usage statistics.
 * Shows current usage vs limits for gyms, members, and staff.
 */
@Data
@Builder
public class OrganisationUsageResponse {

    // Gym usage
    private long currentGyms;
    private int maxGyms;
    private double gymUsagePercent;
    private boolean canAddGym;

    // Member usage
    private long currentMembers;
    private int maxMembers;
    private double memberUsagePercent;
    private boolean canAddMember;

    // Staff usage
    private long currentStaff;
    private int maxStaff;
    private double staffUsagePercent;
    private boolean canAddStaff;

    // Overall status
    private boolean hasUsageWarning;
    private boolean hasLimitReached;

    public static OrganisationUsageResponse fromUsage(OrganisationUsage usage) {
        return OrganisationUsageResponse.builder()
                .currentGyms(usage.currentGyms())
                .maxGyms(usage.maxGyms())
                .gymUsagePercent(usage.gymUsagePercent())
                .canAddGym(usage.canAddGym())
                .currentMembers(usage.currentMembers())
                .maxMembers(usage.maxMembers())
                .memberUsagePercent(usage.memberUsagePercent())
                .canAddMember(usage.canAddMember())
                .currentStaff(usage.currentStaff())
                .maxStaff(usage.maxStaff())
                .staffUsagePercent(usage.staffUsagePercent())
                .canAddStaff(usage.canAddStaff())
                .hasUsageWarning(usage.hasUsageWarning())
                .hasLimitReached(usage.hasLimitReached())
                .build();
    }
}

