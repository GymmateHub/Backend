package com.gymmate.subscription.infrastructure;

import com.gymmate.subscription.domain.GymSubscription;
import com.gymmate.subscription.domain.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymSubscriptionRepository extends JpaRepository<GymSubscription, UUID> {

    Optional<GymSubscription> findByGymId(UUID gymId);

    Optional<GymSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<GymSubscription> findByStripeCustomerId(String stripeCustomerId);

    List<GymSubscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT gs FROM GymSubscription gs WHERE gs.status IN :statuses")
    List<GymSubscription> findByStatuses(@Param("statuses") List<SubscriptionStatus> statuses);

    @Query("SELECT gs FROM GymSubscription gs WHERE gs.currentPeriodEnd < :now AND gs.status = :status")
    List<GymSubscription> findExpiredSubscriptions(
        @Param("now") LocalDateTime now,
        @Param("status") SubscriptionStatus status
    );

    @Query("SELECT gs FROM GymSubscription gs WHERE gs.currentPeriodEnd BETWEEN :start AND :end")
    List<GymSubscription> findSubscriptionsExpiringBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT gs FROM GymSubscription gs WHERE gs.trialEnd BETWEEN :start AND :end AND gs.status = 'TRIAL'")
    List<GymSubscription> findTrialsEndingBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(gs) FROM GymSubscription gs WHERE gs.tier.id = :tierId AND gs.status IN ('ACTIVE', 'TRIAL')")
    Long countActiveSubscriptionsByTier(@Param("tierId") UUID tierId);
}

