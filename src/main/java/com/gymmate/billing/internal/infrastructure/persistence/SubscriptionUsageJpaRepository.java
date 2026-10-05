package com.gymmate.billing.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionUsageJpaRepository extends JpaRepository<SubscriptionUsageJpaEntity, UUID> {

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.subscription.id = :subscriptionId " + "AND su.billingPeriodStart <= :date AND su.billingPeriodEnd > :date")
    Optional<SubscriptionUsageJpaEntity> findBySubscriptionAndPeriod(@Param("subscriptionId") UUID subscriptionId, @Param("date") LocalDateTime date);

    List<SubscriptionUsageJpaEntity> findBySubscriptionId(UUID subscriptionId);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.subscription.organisationId = :organisationId " + "ORDER BY su.billingPeriodStart DESC")
    List<SubscriptionUsageJpaEntity> findByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.isBilled = false " + "AND su.billingPeriodEnd < :now")
    List<SubscriptionUsageJpaEntity> findUnbilledUsage(@Param("now") LocalDateTime now);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.billingPeriodEnd BETWEEN :start AND :end " + "AND su.isBilled = false")
    List<SubscriptionUsageJpaEntity> findUsageForBillingPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
