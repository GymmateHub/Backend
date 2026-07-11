package com.gymmate.admin.api;

import com.gymmate.gym.infrastructure.GymJpaRepository;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Platform-level administration endpoints for GymMateHub itself.
 * Everything here is cross-tenant and restricted to SUPER_ADMIN.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Platform Admin", description = "Cross-tenant platform administration (SUPER_ADMIN only)")
public class PlatformAdminController {

    private final OrganisationRepository organisationRepository;
    private final GymJpaRepository gymRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Platform overview", description = "Cross-tenant counts and recent organisations for the super admin dashboard")
    public ResponseEntity<ApiResponse<PlatformOverview>> getOverview() {
        long totalOrganisations = organisationRepository.count();
        long totalGyms = gymRepository.count();
        long totalUsers = userRepository.count();
        long totalOwners = userRepository.countByRole(UserRole.OWNER);
        long totalMembers = memberRepository.count();

        List<Organisation> recent = organisationRepository
                .findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();

        List<OrganisationSummary> recentOrganisations = recent.stream()
                .map(org -> OrganisationSummary.builder()
                        .id(org.getId())
                        .name(org.getName())
                        .slug(org.getSlug())
                        .contactEmail(org.getContactEmail())
                        .subscriptionPlan(org.getSubscriptionPlan())
                        .subscriptionStatus(org.getSubscriptionStatus())
                        .gymCount(gymRepository.countByOrganisationId(org.getId()))
                        .createdAt(org.getCreatedAt())
                        .build())
                .toList();

        PlatformOverview overview = PlatformOverview.builder()
                .totalOrganisations(totalOrganisations)
                .totalGyms(totalGyms)
                .totalUsers(totalUsers)
                .totalOwners(totalOwners)
                .totalMembers(totalMembers)
                .recentOrganisations(recentOrganisations)
                .build();

        return ResponseEntity.ok(ApiResponse.success(overview, "Platform overview"));
    }

    @GetMapping("/organisations")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "List all organisations", description = "All tenants with owner, gym and member counts (super admin tenant management)")
    public ResponseEntity<ApiResponse<List<TenantSummary>>> listOrganisations() {
        List<TenantSummary> tenants = organisationRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(org -> {
                    // Name-only projection: full User loads can fail on legacy
                    // rows whose role value predates the UserRole enum.
                    String ownerName = null;
                    if (org.getOwnerUserId() != null) {
                        try {
                            ownerName = userRepository.findFullNameById(org.getOwnerUserId()).orElse(null);
                        } catch (Exception e) {
                            log.warn("Could not resolve owner name for organisation {}: {}", org.getId(),
                                    e.getMessage());
                        }
                    }
                    return TenantSummary.builder()
                            .id(org.getId())
                            .name(org.getName())
                            .slug(org.getSlug())
                            .ownerName(ownerName)
                            .contactEmail(org.getContactEmail())
                            .gymCount(gymRepository.countByOrganisationId(org.getId()))
                            .memberCount(memberRepository.countByOrganisationId(org.getId()))
                            .plan(org.getSubscriptionPlan())
                            .status(resolveStatus(org))
                            .createdAt(org.getCreatedAt())
                            .build();
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(tenants, "Organisations"));
    }

    /** Collapse organisation state into the statuses the admin UI filters by. */
    private static String resolveStatus(Organisation org) {
        if (!org.isActive()) {
            return "suspended";
        }
        if ("trial".equalsIgnoreCase(org.getSubscriptionStatus())) {
            return "pending";
        }
        return "active";
    }

    @Builder
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
            LocalDateTime createdAt) {
    }

    @Builder
    public record PlatformOverview(
            long totalOrganisations,
            long totalGyms,
            long totalUsers,
            long totalOwners,
            long totalMembers,
            List<OrganisationSummary> recentOrganisations) {
    }

    @Builder
    public record OrganisationSummary(
            UUID id,
            String name,
            String slug,
            String contactEmail,
            String subscriptionPlan,
            String subscriptionStatus,
            long gymCount,
            LocalDateTime createdAt) {
    }
}
