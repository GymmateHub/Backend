package com.gymmate.shared.multitenancy;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable value object representing tenant identity (organisation ID, gym ID, user ID).
 * Serialized inside domain event payloads to guarantee durable tenancy across async
 * boundaries and Spring Modulith outbox publication replays.
 */
public record TenantIdentity(
        UUID organisationId,
        UUID gymId,
        UUID userId
) implements Serializable {

    public TenantIdentity {
        Objects.requireNonNull(organisationId, "organisationId must not be null for TenantIdentity");
    }

    /**
     * Create a TenantIdentity from explicit IDs.
     */
    public static TenantIdentity of(UUID organisationId, UUID gymId, UUID userId) {
        return new TenantIdentity(organisationId, gymId, userId);
    }

    /**
     * Create a TenantIdentity from explicit organisation ID and gym ID.
     */
    public static TenantIdentity of(UUID organisationId, UUID gymId) {
        return new TenantIdentity(organisationId, gymId, null);
    }

    /**
     * Create a TenantIdentity for organisation-wide operations.
     */
    public static TenantIdentity forOrganisation(UUID organisationId) {
        return new TenantIdentity(organisationId, null, null);
    }

    /**
     * Captures the current thread's {@link TenantContext} into an immutable {@link TenantIdentity}.
     * Throws {@link IllegalStateException} if no tenant context exists on the current thread.
     */
    public static TenantIdentity fromCurrentContext() {
        UUID tenantId = TenantContext.requireCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();
        return new TenantIdentity(tenantId, gymId, null);
    }
}
