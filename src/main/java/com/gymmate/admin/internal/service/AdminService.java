package com.gymmate.admin.internal.service;

import com.gymmate.admin.api.dto.OrganisationSummary;
import com.gymmate.admin.api.dto.PlatformOverview;
import com.gymmate.admin.api.dto.TenantSummary;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
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

    private final OrganisationRepository organisationRepository;
    private final GymRepository gymRepository;
    private final UserRepository userRepository;

    public PlatformOverview getPlatformOverview() {
        long totalOrganisations = organisationRepository.count();
        long totalGyms = gymRepository.count();
        long totalUsers = userRepository.count();
        long totalOwners = userRepository.countByRole(UserRole.GYM_OWNER);
        long totalMembers = userRepository.countByRole(UserRole.MEMBER);

        Page<Organisation> recentOrgPage = organisationRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<OrganisationSummary> recentOrganisations = recentOrgPage.getContent().stream()
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

    private OrganisationSummary mapToSummary(Organisation org) {
        long gymCount = gymRepository.countByOrganisationId(org.getId());
        return OrganisationSummary.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .contactEmail(org.getContactEmail())
                .subscriptionPlan(org.getSubscriptionPlan())
                .subscriptionStatus(org.getSubscriptionStatus())
                .gymCount(gymCount)
                .createdAt(org.getCreatedAt())
                .build();
    }

    public List<TenantSummary> getOrganisations() {
        return organisationRepository.findAll().stream()
                .map(this::mapToTenantSummary)
                .collect(Collectors.toList());
    }

    private TenantSummary mapToTenantSummary(Organisation org) {
        long gymCount = gymRepository.countByOrganisationId(org.getId());
        long memberCount = userRepository.countByOrganisationIdAndRole(org.getId(), UserRole.MEMBER);
        
        String ownerName = null;
        if (org.getOwnerUserId() != null) {
            ownerName = userRepository.findById(org.getOwnerUserId())
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse(null);
        }

        String status = "pending";
        if (Boolean.TRUE.equals(org.isActive())) {
            status = "active";
        } else if (org.getSubscriptionStatus() != null && org.getSubscriptionStatus().equals("suspended")) {
            status = "suspended";
        }

        return TenantSummary.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .ownerName(ownerName)
                .contactEmail(org.getContactEmail())
                .gymCount(gymCount)
                .memberCount(memberCount)
                .plan(org.getSubscriptionPlan())
                .status(status)
                .createdAt(org.getCreatedAt())
                .build();
    }
}
