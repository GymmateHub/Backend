package com.gymmate.identity.api.dto;

import java.util.UUID;

/** Tenant claims carried by an access token. */
public record AccessTokenClaims(UUID userId, UUID organisationId, UUID gymId) {
}
