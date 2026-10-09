package com.gymmate.whitelabel.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;

import java.util.Optional;
import java.util.UUID;

public interface WhitelabelSettingsRepository extends DomainRepository<WhitelabelSettings, UUID> {
    Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId);
    Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);
    Optional<WhitelabelSettings> findByGymId(UUID gymId);
}
