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
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrganisationId(UUID organisationId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT os FROM Subscription os WHERE os.status IN :statuses")
    List<Subscription> findByStatuses(@Param("statuses") List<SubscriptionStatus> statuses);

    @Query("SELECT os FROM Subscription os WHERE os.currentPeriodEnd < :now AND os.status = :status")
    List<Subscription> findExpiredSubscriptions(
        @Param("now") LocalDateTime now,
        @Param("status") SubscriptionStatus status
    );

    @Query("SELECT os FROM Subscription os WHERE os.currentPeriodEnd BETWEEN :start AND :end")
    List<Subscription> findSubscriptionsExpiringBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT os FROM Subscription os WHERE os.trialEnd BETWEEN :start AND :end AND os.status = 'TRIAL'")
    List<Subscription> findTrialsEndingBetween(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT os FROM Subscription os WHERE os.cancelAtPeriodEnd = true AND os.currentPeriodEnd < :date")
    List<Subscription> findCancelledSubscriptionsToProcess(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(os) FROM Subscription os WHERE os.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);

    boolean existsByOrganisationId(UUID organisationId);
}

