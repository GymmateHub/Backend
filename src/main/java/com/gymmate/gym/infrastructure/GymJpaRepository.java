package com.gymmate.gym.infrastructure;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Gym entity.
 * Provides multi-tenant aware queries.
 */
@Repository
public interface GymJpaRepository extends JpaRepository<Gym, UUID> {

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
    @Query("SELECT COUNT(g) FROM Gym g WHERE g.organisationId = :organisationId")
    long countByOrganisationId(@Param("organisationId") UUID organisationId);

    /**
     * Count active gyms in an organisation.
     */
    @Query("SELECT COUNT(g) FROM Gym g WHERE g.organisationId = :organisationId AND g.status = :status")
    long countByOrganisationIdAndStatus(@Param("organisationId") UUID organisationId,
            @Param("status") GymStatus status);

    /**
     * Sum of maxMembers across all gyms in an organisation.
     */
    @Query("SELECT COALESCE(SUM(g.maxMembers), 0) FROM Gym g WHERE g.organisationId = :organisationId")
    Integer sumMaxMembersByOrganisationId(@Param("organisationId") UUID organisationId);

    /**
     * Find gym by slug (globally unique).
     */
    Optional<Gym> findBySlug(String slug);

    /**
     * Check if slug exists.
     */
    boolean existsBySlug(String slug);

    // ========== General queries ==========

    List<Gym> findByStatus(GymStatus status);

    List<Gym> findByCity(String city);
}
