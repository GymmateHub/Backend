package com.gymmate.organisation.api.dto;

import com.gymmate.organisation.domain.Organisation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for organisation details.
 */
@Data
@Builder
public class OrganisationResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID ownerUserId;

    // Subscription info
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDateTime subscriptionStartedAt;
    private LocalDateTime subscriptionExpiresAt;
    private LocalDateTime trialEndsAt;

    // Limits
    private Integer maxGyms;
    private Integer maxMembers;
    private Integer maxStaff;

    // Contact
    private String contactEmail;
    private String contactPhone;
    private String billingEmail;

    // Status
    private boolean onboardingCompleted;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrganisationResponse fromEntity(Organisation org) {
        return OrganisationResponse.builder()
                .id(org.getId())
                .name(org.getName())
                .slug(org.getSlug())
                .ownerUserId(org.getOwnerUserId())
                .subscriptionPlan(org.getSubscriptionPlan())
                .subscriptionStatus(org.getSubscriptionStatus())
                .subscriptionStartedAt(org.getSubscriptionStartedAt())
                .subscriptionExpiresAt(org.getSubscriptionExpiresAt())
                .trialEndsAt(org.getTrialEndsAt())
                .maxGyms(org.getMaxGyms())
                .maxMembers(org.getMaxMembers())
                .maxStaff(org.getMaxStaff())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .billingEmail(org.getBillingEmail())
                .onboardingCompleted(org.isOnboardingCompleted())
                .active(org.isActive())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}

