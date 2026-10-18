package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.SubscriptionTier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionTierRepository extends DomainRepository<SubscriptionTier, UUID> {

    Optional<SubscriptionTier> findByName(String name);

    List<SubscriptionTier> findByActiveTrueOrderBySortOrder();

    List<SubscriptionTier> findFeaturedTiers();

    List<SubscriptionTier> findSuitableTiersForMemberCount(Integer memberCount);
}

