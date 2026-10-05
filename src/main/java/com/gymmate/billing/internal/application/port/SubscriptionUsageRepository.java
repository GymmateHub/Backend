package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.SubscriptionUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SubscriptionUsageRepository {

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

SubscriptionUsage save(SubscriptionUsage entity);

List<SubscriptionUsage> saveAll(Iterable<SubscriptionUsage> entities);

Optional<SubscriptionUsage> findById(UUID id);

boolean existsById(UUID id);

List<SubscriptionUsage> findAll();

List<SubscriptionUsage> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(SubscriptionUsage entity);

void deleteAll(Iterable<SubscriptionUsage> entities);

SubscriptionUsage saveAndFlush(SubscriptionUsage entity);

void flush();

Page<SubscriptionUsage> findAll(Pageable pageable);
}

