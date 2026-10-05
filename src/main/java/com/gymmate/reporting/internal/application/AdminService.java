package com.gymmate.reporting.internal.application;

import com.gymmate.reporting.internal.application.dto.OrganisationSummary;
import com.gymmate.reporting.internal.application.dto.PlatformOverview;
import com.gymmate.reporting.internal.application.dto.TenantSummary;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.dto.OrganisationInfo;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.identity.api.IdentityApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final OrganisationApi organisationApi;
    private final IdentityApi identityApi;

    public PlatformOverview getPlatformOverview() {
        long totalOrganisations = organisationApi.countOrganisations();
        long totalGyms = organisationApi.countGyms();
        long totalUsers = identityApi.countUsers();
        long totalOwners = identityApi.countUsersByRole(UserRole.GYM_OWNER);
        long totalMembers = identityApi.countUsersByRole(UserRole.MEMBER);

        List<OrganisationSummary> recentOrganisations = organisationApi.listRecentOrganisations(5).stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return PlatformOverview.builder()
                .totalOrganisations(totalOrganisations)
                .totalGyms(totalGyms)
                .totalUsers(totalUsers)
                .totalOwners(totalOwners)
                .totalMembers(totalMembers)
                .recentOrganisations(recentOrganisations)
                .build();
    }

    private OrganisationSummary mapToSummary(OrganisationInfo org) {
        long gymCount = organisationApi.countGymsByOrganisation(org.id());
        return OrganisationSummary.builder()
                .id(org.id())
                .name(org.name())
                .slug(org.slug())
                .contactEmail(org.contactEmail())
                .subscriptionPlan(org.subscriptionPlan())
                .subscriptionStatus(org.subscriptionStatus())
                .gymCount(gymCount)
                .createdAt(org.createdAt())
                .build();
    }

    public List<TenantSummary> getOrganisations() {
        return organisationApi.listOrganisations().stream()
                .map(this::mapToTenantSummary)
                .collect(Collectors.toList());
    }

    private TenantSummary mapToTenantSummary(OrganisationInfo org) {
        long gymCount = organisationApi.countGymsByOrganisation(org.id());
        long memberCount = identityApi.countUsersByOrganisationAndRole(org.id(), UserRole.MEMBER);
        
        String ownerName = null;
        if (org.ownerUserId() != null) {
            ownerName = identityApi.findUser(org.ownerUserId())
                .map(u -> u.firstName() + " " + u.lastName())
                .orElse(null);
        }

        String status = "pending";
        if (Boolean.TRUE.equals(org.active())) {
            status = "active";
        } else if (org.subscriptionStatus() != null && org.subscriptionStatus().equals("suspended")) {
            status = "suspended";
        }

        return TenantSummary.builder()
                .id(org.id())
                .name(org.name())
                .slug(org.slug())
                .ownerName(ownerName)
                .contactEmail(org.contactEmail())
                .gymCount(gymCount)
                .memberCount(memberCount)
                .plan(org.subscriptionPlan())
                .status(status)
                .createdAt(org.createdAt())
                .build();
    }
}
