package com.gymmate.membership.internal.infrastructure.persistence;

import java.math.BigDecimal;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import com.gymmate.membership.internal.domain.FreezePolicy;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link FreezePolicy} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "FreezePolicy")
@Table(name = "freeze_policies")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(FreezePolicy.class)
public class FreezePolicyJpaEntity extends GymScopedJpaEntity {

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(name = "max_freeze_days_per_year")
    private Integer maxFreezeDaysPerYear = 90; // Default: 90 days per year

    @Column(name = "max_consecutive_freeze_days")
    private Integer maxConsecutiveFreezeDays = 60; // Default: max 60 days per freeze

    @Column(name = "min_membership_days_before_freeze")
    private Integer minMembershipDaysBeforeFreeze = 30; // Default: must be member for 30 days

    @Column(name = "cooling_off_period_days")
    private Integer coolingOffPeriodDays = 30; // Default: 30 days between freezes

    @Column(name = "freeze_fee_amount", precision = 10, scale = 2)
    private BigDecimal freezeFeeAmount = BigDecimal.ZERO; // Default: no fee

    @Column(name = "freeze_fee_frequency", length = 20)
    private String freezeFeeFrequency = "NONE"; // NONE, ONE_TIME, MONTHLY

    @Column(name = "allow_partial_month_freeze")
    private Boolean allowPartialMonthFreeze = true;

    @Column(name = "is_default_policy")
    private Boolean isDefaultPolicy = false;
}
