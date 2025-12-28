package com.gymmate.subscription.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, UUID> {

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.subscription.id = :subscriptionId " +
           "AND su.billingPeriodStart <= :date AND su.billingPeriodEnd > :date")
    Optional<SubscriptionUsage> findBySubscriptionAndPeriod(
        @Param("subscriptionId") UUID subscriptionId,
        @Param("date") LocalDateTime date
    );

    List<SubscriptionUsage> findBySubscriptionId(UUID subscriptionId);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.subscription.gymId = :gymId " +
           "ORDER BY su.billingPeriodStart DESC")
    List<SubscriptionUsage> findByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.isBilled = false " +
           "AND su.billingPeriodEnd < :now")
    List<SubscriptionUsage> findUnbilledUsage(@Param("now") LocalDateTime now);

    @Query("SELECT su FROM SubscriptionUsage su WHERE su.billingPeriodEnd BETWEEN :start AND :end " +
           "AND su.isBilled = false")
    List<SubscriptionUsage> findUsageForBillingPeriod(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

