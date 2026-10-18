package com.gymmate.organisation.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.constants.GymStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Gym aggregate.
 * Provides multi-tenant aware operations.
 */
public interface GymRepository extends DomainRepository<Gym, UUID> {

    // ========== Organisation-based queries (preferred) ==========

    /**
     * Find all gyms belonging to an organisation.
     */
    List<Gym> findByOrganisationId(UUID organisationId);

    /**
     * Find all active gyms belonging to an organisation.
     */
    List<Gym> findByOrganisationIdAndStatus(UUID organisationId, GymStatus status);

    /**
     * Count gyms in an organisation.
     */
    long countByOrganisationId(UUID organisationId);

    /**
     * Find gym by slug.
     */
    Optional<Gym> findBySlug(String slug);

    // ========== General queries ==========

    List<Gym> findByStatus(GymStatus status);

    List<Gym> findByAddressCity(String city);

    long countByOrganisationIdAndStatus(UUID organisationId, GymStatus status);

    Integer sumMaxMembersByOrganisationId(UUID organisationId);

    boolean existsBySlug(String slug);

    List<Gym> findByCity(String city);
}
