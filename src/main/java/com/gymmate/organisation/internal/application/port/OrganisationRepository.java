package com.gymmate.organisation.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.organisation.internal.domain.Organisation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Organisation entity.
 * Provides data access methods for organisation management.
 */
public interface OrganisationRepository extends DomainRepository<Organisation, UUID> {

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
}

