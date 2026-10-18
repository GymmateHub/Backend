package com.gymmate.organisation.api.dto;

import java.util.UUID;

/** Creates the first gym of a freshly registered organisation. */
public record InitialGymCommand(
        UUID organisationId,
        UUID ownerUserId,
        String name,
        String description,
        String email,
        String phone,
        String timezone,
        String currency,
        String country) {
}
