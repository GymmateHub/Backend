package com.gymmate.organisation.internal.infrastructure.integration;

import com.gymmate.organisation.internal.application.port.GymRepository;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.identity.api.spi.GymDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implements user's {@link GymDirectory} port using this module's own
 * {@code GymRepository} — see the port package Javadoc for why {@code InviteService}
 * no longer calls {@code gym.application.GymService} directly.
 */
@Component
@RequiredArgsConstructor
public class UserGymDirectoryAdapter implements GymDirectory {

    private final GymRepository gymRepository;

    @Override
    public GymSummary getGymSummary(UUID gymId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));
        return new GymSummary(gym.getId(), gym.getName(), gym.getOrganisationId());
    }

    @Override
    public java.util.Optional<UUID> findDefaultActiveGymId(UUID organisationId) {
        return gymRepository.findByOrganisationIdAndStatus(organisationId, com.gymmate.shared.constants.GymStatus.ACTIVE)
                .stream().findFirst().map(Gym::getId);
    }
}
