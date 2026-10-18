package com.gymmate.organisation.internal.infrastructure.persistence;

import com.gymmate.shared.constants.GymStatus;
import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.gymmate.shared.domain.Strings;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.organisation.internal.domain.Gym;
import lombok.Getter;
import lombok.Setter;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Gym} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Gym")
@Table(name = "gyms")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Gym.class)
public class GymJpaEntity extends TenantJpaEntity {

    // Note: organisationId is inherited from TenantJpaEntity
    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Address fields (flattened from Address value object)
    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    // Contact information
    @Column(length = 20)
    private String phone;

    @Column
    private String email;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    // Business settings
    @Column(length = 50)
    private String timezone = "UTC";

    @Column(length = 3)
    private String currency = "USD";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_hours", columnDefinition = "jsonb")
    private String businessHours;

    // Note: Subscription is now at Organisation level, but gyms may have specific
    // features
    @Column(name = "subscription_plan", length = 50)
    private String subscriptionPlan = "starter";

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 20)
    private GymStatus status = GymStatus.ACTIVE;

    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;

    @Column(name = "max_members")
    private Integer maxMembers = 200;

    // Stripe Connect fields for receiving member payments
    @Column(name = "stripe_connect_account_id")
    private String stripeConnectAccountId;

    @Column(name = "stripe_charges_enabled")
    private Boolean stripeChargesEnabled = false;

    @Column(name = "stripe_payouts_enabled")
    private Boolean stripePayoutsEnabled = false;

    @Column(name = "stripe_details_submitted")
    private Boolean stripeDetailsSubmitted = false;

    @Column(name = "stripe_onboarding_completed_at")
    private LocalDateTime stripeOnboardingCompletedAt;

    // Features enabled
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features_enabled", columnDefinition = "jsonb")
    private String featuresEnabled = "[]";

    // Status
    @Column(name = "onboarding_completed")
    private boolean onboardingCompleted = false;
}
