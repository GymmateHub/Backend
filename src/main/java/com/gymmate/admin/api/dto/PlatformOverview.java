package com.gymmate.admin.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlatformOverview {
    private long totalOrganisations;
    private long totalGyms;
    private long totalUsers;
    private long totalOwners;
    private long totalMembers;
    private List<OrganisationSummary> recentOrganisations;
}
