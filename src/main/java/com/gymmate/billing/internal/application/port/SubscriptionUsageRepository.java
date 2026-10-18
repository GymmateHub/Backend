package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.SubscriptionUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionUsageRepository extends DomainRepository<SubscriptionUsage, UUID> {

    Optional<SubscriptionUsage> findBySubscriptionAndPeriod(
        UUID subscriptionId,
        LocalDateTime date
    );

    List<SubscriptionUsage> findBySubscriptionId(UUID subscriptionId);

    List<SubscriptionUsage> findByOrganisationId(UUID organisationId);

    List<SubscriptionUsage> findUnbilledUsage(LocalDateTime now);

    List<SubscriptionUsage> findUsageForBillingPeriod(
        LocalDateTime start,
        LocalDateTime end
    );
}

