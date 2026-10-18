package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.identity.internal.domain.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Trainer entity.
 */
public interface TrainerRepository extends DomainRepository<Trainer, UUID> {

    // User lookup
    Optional<Trainer> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // ========== Organisation-scoped queries (preferred) ==========

    List<Trainer> findByOrganisationId(UUID organisationId);

    List<Trainer> findActiveAndAcceptingClientsByOrganisationId(UUID organisationId);

    List<Trainer> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType);

    // ========== Legacy unscoped queries (use org-scoped variants instead) ==========

    // Active trainers
    List<Trainer> findActiveAndAcceptingClients();

    // Accepting clients
    List<Trainer> findByAcceptingClients(boolean accepting);

    // Employment type
    List<Trainer> findByEmploymentType(String employmentType);
}
