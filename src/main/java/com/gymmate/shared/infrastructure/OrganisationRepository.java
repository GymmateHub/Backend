package com.gymmate.shared.infrastructure;

import com.gymmate.shared.domain.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Optional<Organisation> findBySlug(String slug);

    Optional<Organisation> findByOwnerUserId(UUID ownerUserId);

    boolean existsBySlug(String slug);

    boolean existsByOwnerUserId(UUID ownerUserId);
}

