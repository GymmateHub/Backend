package com.gymmate.organisation.internal.application.dto;

import com.gymmate.organisation.internal.domain.Organisation;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for organisation details.
 */
public record OrganisationResponse(
        UUID id,
        String name,
        String slug,
        UUID ownerUserId,
        // Subscription info
        String subscriptionPlan,
        String subscriptionStatus,
        LocalDateTime subscriptionStartedAt,
        LocalDateTime subscriptionExpiresAt,
        LocalDateTime trialEndsAt,
        // Limits
        Integer maxGyms,
        Integer maxMembers,
        Integer maxStaff,
        // Contact
        String contactEmail,
        String contactPhone,
        String billingEmail,
        // Status
        boolean onboardingCompleted,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static OrganisationResponse fromEntity(Organisation org) {
        return new OrganisationResponse(
                org.getId(),
                org.getName(),
                org.getSlug(),
                org.getOwnerUserId(),
                org.getSubscriptionPlan(),
                org.getSubscriptionStatus(),
                org.getSubscriptionStartedAt(),
                org.getSubscriptionExpiresAt(),
                org.getTrialEndsAt(),
                org.getMaxGyms(),
                org.getMaxMembers(),
                org.getMaxStaff(),
                org.getContactEmail(),
                org.getContactPhone(),
                org.getBillingEmail(),
                org.isOnboardingCompleted(),
                org.isActive(),
                org.getCreatedAt(),
                org.getUpdatedAt());
    }
}
