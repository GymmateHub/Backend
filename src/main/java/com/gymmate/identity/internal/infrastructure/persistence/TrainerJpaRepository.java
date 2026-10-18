package com.gymmate.identity.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Trainer entity.
 */
@Repository
public interface TrainerJpaRepository extends JpaRepository<TrainerJpaEntity, UUID> {

    // User lookup
    Optional<TrainerJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    // ========== Organisation-scoped queries (preferred) ==========
    List<TrainerJpaEntity> findByOrganisationId(UUID organisationId);

    @Query("SELECT t FROM Trainer t WHERE t.organisationId = :organisationId AND t.active = true AND t.acceptingClients = true")
    List<TrainerJpaEntity> findActiveAndAcceptingClientsByOrganisationId(@Param("organisationId") UUID organisationId);

    List<TrainerJpaEntity> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType);

    // ========== Legacy unscoped queries (use org-scoped variants instead) ==========
    // Active trainers
    @Query("SELECT t FROM Trainer t WHERE t.active = true AND t.acceptingClients = true")
    List<TrainerJpaEntity> findActiveAndAcceptingClients();

    // Accepting clients
    List<TrainerJpaEntity> findByAcceptingClients(boolean accepting);

    // Employment type
    List<TrainerJpaEntity> findByEmploymentType(String employmentType);
}
