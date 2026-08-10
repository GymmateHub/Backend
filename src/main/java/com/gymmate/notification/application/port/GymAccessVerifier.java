package com.gymmate.notification.application.port;

import java.util.UUID;

/**
 * Verifies a gym belongs to a given organisation before serving org-scoped
 * notification queries — see the port package Javadoc for why this is a port
 * ({@code NotificationController} used to read {@code gym.infrastructure.GymRepository}
 * directly, which was the sole {@code notification -> gym} edge closing a module
 * dependency cycle).
 */
public interface GymAccessVerifier {

    /**
     * @throws com.gymmate.shared.exception.ResourceNotFoundException if the gym doesn't exist
     * @throws com.gymmate.shared.exception.DomainException if the gym doesn't belong to organisationId
     */
    void verifyGymBelongsToOrganisation(UUID gymId, UUID organisationId);
}
