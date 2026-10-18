package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

    Optional<SubscriptionJpaEntity> findByOrganisationId(UUID organisationId);

    Optional<SubscriptionJpaEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<SubscriptionJpaEntity> findByStripeCustomerId(String stripeCustomerId);

    List<SubscriptionJpaEntity> findByStatus(SubscriptionStatus status);

    @Query("SELECT os FROM Subscription os WHERE os.status IN :statuses")
    List<SubscriptionJpaEntity> findByStatuses(@Param("statuses") List<SubscriptionStatus> statuses);

    @Query("SELECT os FROM Subscription os WHERE os.currentPeriodEnd < :now AND os.status = :status")
    List<SubscriptionJpaEntity> findExpiredSubscriptions(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);

    @Query("SELECT os FROM Subscription os WHERE os.currentPeriodEnd BETWEEN :start AND :end")
    List<SubscriptionJpaEntity> findSubscriptionsExpiringBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT os FROM Subscription os WHERE os.trialEnd BETWEEN :start AND :end AND os.status = 'TRIAL'")
    List<SubscriptionJpaEntity> findTrialsEndingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT os FROM Subscription os WHERE os.cancelAtPeriodEnd = true AND os.currentPeriodEnd < :date")
    List<SubscriptionJpaEntity> findCancelledSubscriptionsToProcess(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(os) FROM Subscription os WHERE os.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);

    /**
     * Subscriptions past due for longer than the grace period — candidates for
     * escalation to SUSPENDED. See Subscription.PAST_DUE_GRACE_PERIOD_DAYS.
     */
    @Query("SELECT os FROM Subscription os WHERE os.status = 'PAST_DUE' AND os.pastDueSince < :cutoff")
    List<SubscriptionJpaEntity> findStalePastDueSubscriptions(@Param("cutoff") LocalDateTime cutoff);

    boolean existsByOrganisationId(UUID organisationId);
}
