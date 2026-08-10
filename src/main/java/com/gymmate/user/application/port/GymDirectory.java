package com.gymmate.user.application.port;

import java.util.UUID;

/**
 * The minimal gym data {@code user} module code needs (invite emails) — see the port
 * package Javadoc for why this exists instead of a direct {@code gym} module call.
 */
public interface GymDirectory {

    GymSummary getGymSummary(UUID gymId);

    record GymSummary(UUID id, String name, UUID organisationId) {
    }
}
