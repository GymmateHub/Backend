package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.organisation.internal.domain.Organisation;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Organisation} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Organisation")
@Table(name = "organisations")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Organisation.class)
public class OrganisationJpaEntity extends BaseAuditJpaEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    // Subscription & Billing
    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan = "starter";

    @Column(name = "subscription_status", length = 20)
    private String subscriptionStatus = "trial";

    @Column(name = "subscription_started_at")
    private LocalDateTime subscriptionStartedAt;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;

    // Plan Limits
    @Column(name = "max_gyms")
    private Integer maxGyms = 1;

    @Column(name = "max_members")
    private Integer maxMembers = 200;

    @Column(name = "max_staff")
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
    private String featuresEnabled = "[]";

    // Contact
    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    // Status - Note: isActive is inherited from BaseAuditJpaEntity as 'active' field
    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;

    // Settings
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String settings = "{}";
}
