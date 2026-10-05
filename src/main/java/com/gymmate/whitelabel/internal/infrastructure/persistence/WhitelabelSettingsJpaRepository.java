package com.gymmate.whitelabel.internal.infrastructure.persistence;

import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhitelabelSettingsJpaRepository extends JpaRepository<WhitelabelSettings, UUID> {

    Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId);

    Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

    Optional<WhitelabelSettings> findByGymId(UUID gymId);
}
