package com.gymmate.identity.internal.application.port;

import com.gymmate.identity.internal.domain.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Spring Data JPA repository for Trainer entity.
 */
public interface TrainerRepository {

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
    
    Trainer save(Trainer entity);
    
    List<Trainer> saveAll(Iterable<Trainer> entities);
    
    Optional<Trainer> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<Trainer> findAll();
    
    List<Trainer> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(Trainer entity);
    
    void deleteAll(Iterable<Trainer> entities);
    
    Trainer saveAndFlush(Trainer entity);
    
    void flush();
    
    Page<Trainer> findAll(Pageable pageable);
}
