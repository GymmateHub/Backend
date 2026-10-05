package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.shared.constants.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SubscriptionRepository {

    Optional<Subscription> findByOrganisationId(UUID organisationId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByStatuses(List<SubscriptionStatus> statuses);

    List<Subscription> findExpiredSubscriptions(
        LocalDateTime now,
        SubscriptionStatus status
    );

    List<Subscription> findSubscriptionsExpiringBetween(
        LocalDateTime start,
        LocalDateTime end
    );

    List<Subscription> findTrialsEndingBetween(
        LocalDateTime start,
        LocalDateTime end
    );

    List<Subscription> findCancelledSubscriptionsToProcess(LocalDateTime date);

    long countByStatus(SubscriptionStatus status);

    /**
     * Subscriptions past due for longer than the grace period — candidates for
     * escalation to SUSPENDED. See Subscription.PAST_DUE_GRACE_PERIOD_DAYS.
     */
    List<Subscription> findStalePastDueSubscriptions(LocalDateTime cutoff);

    boolean existsByOrganisationId(UUID organisationId);
    
    Subscription save(Subscription entity);
    
    List<Subscription> saveAll(Iterable<Subscription> entities);
    
    Optional<Subscription> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<Subscription> findAll();
    
    List<Subscription> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(Subscription entity);
    
    void deleteAll(Iterable<Subscription> entities);
    
    Subscription saveAndFlush(Subscription entity);
    
    void flush();
    
    Page<Subscription> findAll(Pageable pageable);
}

