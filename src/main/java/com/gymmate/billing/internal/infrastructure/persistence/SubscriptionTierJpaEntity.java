package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import com.gymmate.billing.internal.domain.SubscriptionTier;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link SubscriptionTier} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "SubscriptionTier")
@Table(name = "subscription_tiers")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(SubscriptionTier.class)
public class SubscriptionTierJpaEntity extends BaseAuditJpaEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String billingCycle = "monthly"; // monthly, annual

    @Column(name = "is_active")
    private Boolean active = true;

    @Column(name = "is_featured")
    private Boolean featured = false;

    // Limits
    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "max_locations", nullable = false)
    private Integer maxLocations = 1;

    @Column(name = "max_staff")
    private Integer maxStaff;

    @Column(name = "max_classes_per_month")
    private Integer maxClassesPerMonth;

    // API Rate Limits
    @Column(name = "api_requests_per_hour", nullable = false)
    private Integer apiRequestsPerHour = 1000;

    @Column(name = "api_burst_limit", nullable = false)
    private Integer apiBurstLimit = 100;

    @Column(name = "concurrent_connections", nullable = false)
    private Integer concurrentConnections = 10;

    // Communication Limits
    @Column(name = "sms_credits_per_month")
    private Integer smsCreditsPerMonth = 0;

    @Column(name = "email_credits_per_month")
    private Integer emailCreditsPerMonth = 0;

    // Feature Flags
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String features = "[]";

    // Overage Pricing
    @Column(name = "overage_member_price", precision = 10, scale = 2)
    private BigDecimal overageMemberPrice = new BigDecimal("2.00");

    @Column(name = "overage_sms_price", precision = 10, scale = 2)
    private BigDecimal overageSmsPrice = new BigDecimal("0.05");

    @Column(name = "overage_email_price", precision = 10, scale = 2)
    private BigDecimal overageEmailPrice = new BigDecimal("0.02");

    // Metadata
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    // Stripe Integration
    @Column(name = "stripe_product_id")
    private String stripeProductId;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "trial_days")
    private Integer trialDays = 14;
}
