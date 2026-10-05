package com.gymmate.organisation.internal.application.port;

import com.gymmate.organisation.internal.domain.Organisation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * JPA Repository for Organisation entity.
 * Provides data access methods for organisation management.
 */
public interface OrganisationRepository {

    Optional<Organisation> findBySlug(String slug);

    Optional<Organisation> findByOwnerUserId(UUID ownerUserId);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    List<Organisation> findAllActive();

    List<Organisation> findBySubscriptionStatus(String status);

    List<Organisation> findTrialsEndingBefore(LocalDateTime date);

    List<Organisation> findSubscriptionsExpiringBetween(
        LocalDateTime start,
        LocalDateTime end
    );

    long countActive();

    long countBySubscriptionStatus(String status);

    List<Organisation> findTrialsEndingBetween(
        LocalDateTime start,
        LocalDateTime end
    );

Organisation save(Organisation entity);

List<Organisation> saveAll(Iterable<Organisation> entities);

Optional<Organisation> findById(UUID id);

boolean existsById(UUID id);

List<Organisation> findAll();

List<Organisation> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(Organisation entity);

void deleteAll(Iterable<Organisation> entities);

Organisation saveAndFlush(Organisation entity);

void flush();

Page<Organisation> findAll(Pageable pageable);
}

