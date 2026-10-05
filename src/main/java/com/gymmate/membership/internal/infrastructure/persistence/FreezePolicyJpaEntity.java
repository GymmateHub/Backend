package com.gymmate.membership.internal.infrastructure.persistence;

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
    private Integer // Default: 90 days per year
    maxFreezeDaysPerYear = 90;

    @Column(name = "max_consecutive_freeze_days")
    private Integer // Default: max 60 days per freeze
    maxConsecutiveFreezeDays = 60;

    @Column(name = "min_membership_days_before_freeze")
    private Integer // Default: must be member for 30 days
    minMembershipDaysBeforeFreeze = 30;

    @Column(name = "cooling_off_period_days")
    private Integer // Default: 30 days between freezes
    coolingOffPeriodDays = 30;

    @Column(name = "freeze_fee_amount")
    private Double // Default: no fee
    freezeFeeAmount = 0.0;

    @Column(name = "freeze_fee_frequency")
    private String // NONE, ONE_TIME, MONTHLY
    freezeFeeFrequency = "NONE";

    @Column(name = "allow_partial_month_freeze")
    private Boolean allowPartialMonthFreeze = true;

    @Column(name = "is_default_policy")
    private Boolean isDefaultPolicy = false;
}
