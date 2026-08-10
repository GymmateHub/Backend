package com.gymmate.shared.integration;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.notification.application.port.GymAccessVerifier;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implements notification's {@link GymAccessVerifier} port. Lives in {@code shared}
 * rather than {@code gym.infrastructure}: putting it in {@code gym} would make
 * {@code gym} depend on {@code notification}, and {@code notification} separately
 * depends on {@code organisation} (via {@code BroadcastService}), which already
 * (correctly) depends on {@code gym} — a 3-module cycle. {@code shared} is declared
 * {@code Type.OPEN} and is exempt from Modulith's cycle detection, which is exactly
 * the escape valve this kind of cross-cutting glue needs; the existing
 * {@code shared.security.service.AuthenticationService} reaching into {@code user}
 * directly is the same established pattern.
 */
@Component
@RequiredArgsConstructor
public class NotificationGymAccessVerifierAdapter implements GymAccessVerifier {

    private final GymRepository gymRepository;

    @Override
    public void verifyGymBelongsToOrganisation(UUID gymId, UUID organisationId) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));
        if (!gym.getOrganisationId().equals(organisationId)) {
            throw new DomainException("ACCESS_DENIED", "You do not have access to this gym");
        }
    }
}
