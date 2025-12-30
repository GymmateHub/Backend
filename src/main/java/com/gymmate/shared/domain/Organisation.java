package com.gymmate.shared.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organisation entity representing a tenant in the multi-tenant SaaS architecture.
 * An organisation can own multiple gyms and has its own subscription/billing.
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

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

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
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
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
        return isActive &&
               ("active".equals(subscriptionStatus) || isTrialActive());
    }

    public boolean canAddGym() {
        return isActive &&
               (maxGyms == null || maxGyms == -1); // -1 = unlimited
    }

    public boolean canAddMember(int currentMemberCount) {
        return isActive &&
               (maxMembers == null || maxMembers == -1 || currentMemberCount < maxMembers);
    }
}

