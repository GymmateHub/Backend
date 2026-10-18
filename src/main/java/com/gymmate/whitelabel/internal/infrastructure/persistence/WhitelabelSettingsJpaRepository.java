package com.gymmate.whitelabel.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WhitelabelSettingsJpaRepository extends JpaRepository<WhitelabelSettingsJpaEntity, UUID> {

    Optional<WhitelabelSettingsJpaEntity> findByOrganisationIdAndGymIdIsNull(UUID organisationId);

    Optional<WhitelabelSettingsJpaEntity> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

    Optional<WhitelabelSettingsJpaEntity> findByGymId(UUID gymId);
}
