package com.gymmate.whitelabel.infrastructure;

import com.gymmate.whitelabel.domain.WhitelabelSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WhitelabelSettingsRepositoryAdapter implements WhitelabelSettingsRepository {

    private final WhitelabelSettingsJpaRepository jpaRepository;

    @Override
    public WhitelabelSettings save(WhitelabelSettings settings) {
        return jpaRepository.save(settings);
    }

    @Override
    public Optional<WhitelabelSettings> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId) {
        return jpaRepository.findByOrganisationIdAndGymIdIsNull(organisationId);
    }

    @Override
    public Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
        return jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId);
    }

    @Override
    public Optional<WhitelabelSettings> findByGymId(UUID gymId) {
        return jpaRepository.findByGymId(gymId);
    }
}
