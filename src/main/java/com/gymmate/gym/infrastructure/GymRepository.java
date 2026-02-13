package com.gymmate.gym.infrastructure;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Gym aggregate.
 * Provides multi-tenant aware operations.
 */
public interface GymRepository {

    Gym save(Gym gym);

    Optional<Gym> findById(UUID id);

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

    List<Gym> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
