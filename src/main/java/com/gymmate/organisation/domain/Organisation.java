package com.gymmate.organisation.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organisation entity representing a tenant in the multi-tenant SaaS architecture.
 * An organisation can own multiple gyms and has its own subscription/billing.
 *
 * This is the root aggregate for multi-tenancy - all gyms, members, and resources
 * belong to an organisation.
 */
@Entity
@Table(name = "organisations")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Organisation extends BaseAuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    // Subscription & Billing
    @Column(name = "subscription_plan", length = 50)
    @Builder.Default
    private String subscriptionPlan = "starter";

    @Column(name = "subscription_status", length = 20)
    @Builder.Default
    private String subscriptionStatus = "trial";

    @Column(name = "subscription_started_at")
    private LocalDateTime subscriptionStartedAt;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    // Plan Limits
    @Column(name = "max_gyms")
    @Builder.Default
    private Integer maxGyms = 1;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 200;

    @Column(name = "max_staff")
    @Builder.Default
    private Integer maxStaff = 10;

    // Billing
    @Column(name = "billing_email")
    private String billingEmail;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_address", columnDefinition = "jsonb")
    private String billingAddress;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    // Features
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features_enabled", columnDefinition = "jsonb")
    @Builder.Default
    private String featuresEnabled = "[]";

    // Contact
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // Status - Note: isActive is inherited from BaseAuditEntity as 'active' field
    @Column(name = "onboarding_completed")
    @Builder.Default
    private boolean onboardingCompleted = false;

    // Settings
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
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

