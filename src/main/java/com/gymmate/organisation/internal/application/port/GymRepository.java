package com.gymmate.organisation.internal.application.port;

import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.constants.GymStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    long count();

    boolean existsById(UUID id);
    
    long countByOrganisationIdAndStatus(UUID organisationId, GymStatus status);
    
    Integer sumMaxMembersByOrganisationId(UUID organisationId);
    
    boolean existsBySlug(String slug);
    
    List<Gym> findByCity(String city);
    
    List<Gym> saveAll(Iterable<Gym> entities);
    
    List<Gym> findAllById(Iterable<UUID> ids);
    
    void delete(Gym entity);
    
    void deleteAll(Iterable<Gym> entities);
    
    Gym saveAndFlush(Gym entity);
    
    void flush();
    
    Page<Gym> findAll(Pageable pageable);
}
