package com.gymmate.organisation.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organisation entity representing a tenant in the multi-tenant SaaS architecture.
 * An organisation can own multiple gyms and has its own subscription/billing.
 *
 * This is the root aggregate for multi-tenancy - all gyms, members, and resources
 * belong to an organisation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Organisation extends BaseAuditEntity {

    private String name;

    private String slug;

    private UUID ownerUserId;

    // Subscription & Billing
    @Builder.Default
    private 
    String subscriptionPlan = "starter";

    @Builder.Default
    private 
    String subscriptionStatus = "trial";

    private LocalDateTime subscriptionStartedAt;

    private LocalDateTime subscriptionExpiresAt;

    private LocalDateTime trialEndsAt;

    // Plan Limits
    @Builder.Default
    private 
    Integer maxGyms = 1;

    @Builder.Default
    private 
    Integer maxMembers = 200;

    @Builder.Default
    private 
    Integer maxStaff = 10;

    // Billing
    private String billingEmail;

    private 
    String billingAddress;

    private String paymentMethodId;

    // Features

    @Builder.Default
    private String featuresEnabled = "[]";

    // Contact
    private String contactEmail;

    private String contactPhone;

    // Status - Note: isActive is inherited from BaseAuditJpaEntity as 'active' field
    @Builder.Default
    private 
    boolean onboardingCompleted = false;

    // Settings

    @Builder.Default
    private String settings = "{}";

    // Business methods
    public void assignOwner(UUID userId) {
        this.ownerUserId = userId;
    }

    public void activate() {
        this.setActive(true);
    }

    public void deactivate() {
        this.setActive(false);
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void updateSubscription(String plan, String status, LocalDateTime expiresAt) {
        this.subscriptionPlan = plan;
        this.subscriptionStatus = status;
        this.subscriptionExpiresAt = expiresAt;
        if (this.subscriptionStartedAt == null) {
            this.subscriptionStartedAt = LocalDateTime.now();
        }
    }

    public boolean isTrialActive() {
        return "trial".equals(subscriptionStatus) &&
               trialEndsAt != null &&
               trialEndsAt.isAfter(LocalDateTime.now());
    }

    public boolean isSubscriptionActive() {
        return isActive() &&
               ("active".equals(subscriptionStatus) || isTrialActive());
    }

    public boolean canAddGym() {
        return isActive() &&
               (maxGyms == null || maxGyms == -1); // -1 = unlimited
    }

    public boolean canAddMember(int currentMemberCount) {
        return isActive() &&
               (maxMembers == null || maxMembers == -1 || currentMemberCount < maxMembers);
    }

    public void updateLimits(Integer maxGyms, Integer maxMembers, Integer maxStaff) {
        if (maxGyms != null && maxGyms > 0) {
            this.maxGyms = maxGyms;
        }
        if (maxMembers != null && maxMembers > 0) {
            this.maxMembers = maxMembers;
        }
        if (maxStaff != null && maxStaff > 0) {
            this.maxStaff = maxStaff;
        }
    }

    public void updateDetails(String name, String contactEmail, String contactPhone,
                             String billingEmail, String settings) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (contactEmail != null && !contactEmail.isBlank()) {
            this.contactEmail = contactEmail;
        }
        if (contactPhone != null) {
            this.contactPhone = contactPhone;
        }
        if (billingEmail != null && !billingEmail.isBlank()) {
            this.billingEmail = billingEmail;
        }
        if (settings != null) {
            this.settings = settings;
        }
    }
}
