package com.gymmate.subscription.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "subscription_tiers")
public class SubscriptionTier extends BaseAuditEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    @Builder.Default
    private String billingCycle = "monthly"; // monthly, annual

    @Column(name = "is_active")
    @Builder.Default
    private Boolean active = true;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean featured = false;

    // Limits
    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "max_locations", nullable = false)
    @Builder.Default
    private Integer maxLocations = 1;

    @Column(name = "max_staff")
    private Integer maxStaff;

    @Column(name = "max_classes_per_month")
    private Integer maxClassesPerMonth;

    // API Rate Limits
    @Column(name = "api_requests_per_hour", nullable = false)
    @Builder.Default
    private Integer apiRequestsPerHour = 1000;

    @Column(name = "api_burst_limit", nullable = false)
    @Builder.Default
    private Integer apiBurstLimit = 100;

    @Column(name = "concurrent_connections", nullable = false)
    @Builder.Default
    private Integer concurrentConnections = 10;

    // Communication Limits
    @Column(name = "sms_credits_per_month")
    @Builder.Default
    private Integer smsCreditsPerMonth = 0;

    @Column(name = "email_credits_per_month")
    @Builder.Default
    private Integer emailCreditsPerMonth = 0;

    // Feature Flags
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String features = "[]";

    // Overage Pricing
    @Column(name = "overage_member_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overageMemberPrice = new BigDecimal("2.00");

    @Column(name = "overage_sms_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overageSmsPrice = new BigDecimal("0.05");

    @Column(name = "overage_email_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overageEmailPrice = new BigDecimal("0.02");

    // Metadata
    @Column(name = "sort_order")
    @Builder.Default
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
    @Builder.Default
    private Integer trialDays = 14;

    // Business methods
    public boolean hasFeature(String feature) {
        // Parse JSON features array and check if feature exists
        return features != null && features.contains(feature);
    }

    public boolean allowsUnlimitedMembers() {
        return maxMembers >= 999999;
    }

    public boolean isStarterPlan() {
        return "starter".equalsIgnoreCase(name);
    }

    public boolean isProfessionalPlan() {
        return "professional".equalsIgnoreCase(name);
    }

    public boolean isEnterprisePlan() {
        return "enterprise".equalsIgnoreCase(name) || "custom".equalsIgnoreCase(name);
    }

    public BigDecimal calculateOverageCost(Integer memberOverage, Integer smsOverage, Integer emailOverage) {
        BigDecimal cost = BigDecimal.ZERO;

        if (memberOverage != null && memberOverage > 0) {
            cost = cost.add(overageMemberPrice.multiply(new BigDecimal(memberOverage)));
        }

        if (smsOverage != null && smsOverage > 0) {
            cost = cost.add(overageSmsPrice.multiply(new BigDecimal(smsOverage)));
        }

        if (emailOverage != null && emailOverage > 0) {
            cost = cost.add(overageEmailPrice.multiply(new BigDecimal(emailOverage)));
        }

        return cost;
    }
}

