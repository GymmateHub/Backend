package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.SubscriptionTier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SubscriptionTierRepository {

    Optional<SubscriptionTier> findByName(String name);

    List<SubscriptionTier> findByActiveTrueOrderBySortOrder();

    List<SubscriptionTier> findFeaturedTiers();

    List<SubscriptionTier> findSuitableTiersForMemberCount(Integer memberCount);

SubscriptionTier save(SubscriptionTier entity);

List<SubscriptionTier> saveAll(Iterable<SubscriptionTier> entities);

Optional<SubscriptionTier> findById(UUID id);

boolean existsById(UUID id);

List<SubscriptionTier> findAll();

List<SubscriptionTier> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(SubscriptionTier entity);

void deleteAll(Iterable<SubscriptionTier> entities);

SubscriptionTier saveAndFlush(SubscriptionTier entity);

void flush();

Page<SubscriptionTier> findAll(Pageable pageable);
}

