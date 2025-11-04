package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Trainer entity.
 */
@Repository
public interface TrainerRepository extends JpaRepository<Trainer, UUID> {

    // User lookup
    Optional<Trainer> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Active trainers
    @Query("SELECT t FROM Trainer t WHERE t.active = true AND t.acceptingClients = true")
    List<Trainer> findActiveAndAcceptingClients();

    // Accepting clients
    List<Trainer> findByAcceptingClients(boolean accepting);

    // Employment type
    List<Trainer> findByEmploymentType(String employmentType);
}

