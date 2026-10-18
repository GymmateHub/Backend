package com.gymmate.identity.api.spi;

import java.util.UUID;

/**
 * The minimal gym data {@code user} module code needs (invite emails) — see the port
 * package Javadoc for why this exists instead of a direct {@code gym} module call.
 */
public interface GymDirectory {

    GymSummary getGymSummary(UUID gymId);

    /**
     * The gym a user of the organisation lands in after login: the first active gym of
     * the organisation, if any.
     */
    java.util.Optional<UUID> findDefaultActiveGymId(UUID organisationId);

    record GymSummary(UUID id, String name, UUID organisationId) {
    }
}
