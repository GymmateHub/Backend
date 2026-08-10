package com.gymmate.whitelabel.infrastructure;

import com.gymmate.whitelabel.domain.WhitelabelSettings;

import java.util.Optional;
import java.util.UUID;

public interface WhitelabelSettingsRepository {
    WhitelabelSettings save(WhitelabelSettings settings);
    Optional<WhitelabelSettings> findById(UUID id);
    Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId);
    Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);
    Optional<WhitelabelSettings> findByGymId(UUID gymId);
}
