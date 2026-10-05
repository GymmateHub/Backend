package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import com.gymmate.membership.internal.domain.MembershipPlan;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MembershipPlan} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MembershipPlan")
@Table(name = "membership_plans")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MembershipPlan.class)
public class MembershipPlanJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String // monthly, quarterly, yearly, lifetime
    billingCycle;

    @Column(name = "duration_months")
    private Integer // NULL for lifetime
    durationMonths;

    // Features
    @Column(name = "class_credits")
    private Integer // NULL for unlimited
    classCredits;

    @Column(name = "guest_passes")
    private Integer guestPasses = 0;

    @Column(name = "trainer_sessions")
    private Integer trainerSessions = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String // ["pool", "sauna", "parking"]
    amenities = "[]";

    // Restrictions
    @Column(name = "peak_hours_access")
    private boolean peakHoursAccess = true;

    @Column(name = "off_peak_only")
    private boolean offPeakOnly = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "specific_areas", columnDefinition = "jsonb")
    private String // ["main_gym", "pool", "studio"]
    specificAreas;

    // Status
    @Column(name = "is_featured")
    private boolean featured = false;

    // Stripe integration
    @Column(name = "stripe_product_id")
    private String stripeProductId;

    @Column(name = "stripe_price_id")
    private String stripePriceId;
}
