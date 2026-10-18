package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.gymmate.billing.internal.domain.SubscriptionUsage;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link SubscriptionUsage} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "SubscriptionUsage")
@Table(name = "subscription_usage")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(SubscriptionUsage.class)
public class SubscriptionUsageJpaEntity extends BaseAuditJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionJpaEntity subscription;

    // Billing Period
    @Column(name = "billing_period_start", nullable = false)
    private LocalDateTime billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDateTime billingPeriodEnd;

    // Member Usage
    @Column(name = "member_count")
    private Integer memberCount = 0;

    @Column(name = "member_overage")
    private Integer memberOverage = 0;

    // Communication Usage
    @Column(name = "sms_sent")
    private Integer smsSent = 0;

    @Column(name = "sms_overage")
    private Integer smsOverage = 0;

    @Column(name = "email_sent")
    private Integer emailSent = 0;

    @Column(name = "email_overage")
    private Integer emailOverage = 0;

    // API Usage
    @Column(name = "api_requests")
    private Integer apiRequests = 0;

    @Column(name = "api_rate_limit_hits")
    private Integer apiRateLimitHits = 0;

    // Classes
    @Column(name = "classes_created")
    private Integer classesCreated = 0;

    // Storage (in GB)
    @Column(name = "storage_used", precision = 10, scale = 2)
    private BigDecimal storageUsed = BigDecimal.ZERO;

    // Calculated Costs
    @Column(name = "base_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "overage_cost", precision = 10, scale = 2)
    private BigDecimal overageCost = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    // Status
    @Column(name = "is_billed")
    private Boolean isBilled = false;

    @Column(name = "billed_at")
    private LocalDateTime billedAt;
}
