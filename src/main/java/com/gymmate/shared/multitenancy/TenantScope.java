package com.gymmate.shared.multitenancy;

import java.util.UUID;

/**
 * Scoped activation of {@link TenantContext} for code that runs off the servlet
 * request thread (async listeners, scheduled jobs) where the thread-local tenant
 * identity set by {@code TenantFilter} does not exist.
 *
 * <p>Unlike a {@code TaskDecorator} that captures the submitting thread's ambient
 * context, this restores tenant identity explicitly from a value the caller already
 * has (e.g. a domain event's {@code organisationId}, or a method parameter) — the
 * submitting thread may itself have no tenant context (e.g. an unauthenticated Stripe
 * webhook request), so there would be nothing to capture.
 *
 * <p>Usage:
 * <pre>{@code
 * try (TenantScope ignored = TenantScope.activate(event.getOrganisationId(), event.getGymId())) {
 *     // repository reads/writes here are correctly tenant-scoped
 * }
 * }</pre>
 */
public final class TenantScope implements AutoCloseable {

    private final UUID previousTenantId;
    private final UUID previousGymId;

    private TenantScope(UUID previousTenantId, UUID previousGymId) {
        this.previousTenantId = previousTenantId;
        this.previousGymId = previousGymId;
    }

    /**
     * Activate tenant context for the current thread, remembering whatever was
     * previously set so it can be restored on {@link #close()}. {@code gymId} may be
     * null when the caller has no gym-level context (e.g. an org-wide event).
     */
    public static TenantScope activate(UUID organisationId, UUID gymId) {
        TenantScope scope = new TenantScope(TenantContext.getCurrentTenantId(), TenantContext.getCurrentGymId());
        TenantContext.setCurrentTenantId(organisationId);
        if (gymId != null) {
            TenantContext.setCurrentGymId(gymId);
        }
        return scope;
    }

    /**
     * Scoped activation of {@link TenantContext} using a {@link TenantIdentity}.
     */
    public static TenantScope activate(TenantIdentity identity) {
        if (identity == null) {
            return new TenantScope(TenantContext.getCurrentTenantId(), TenantContext.getCurrentGymId());
        }
        return activate(identity.organisationId(), identity.gymId());
    }

    @Override
    public void close() {
        TenantContext.clear();
        if (previousTenantId != null) {
            TenantContext.setCurrentTenantId(previousTenantId);
        }
        if (previousGymId != null) {
            TenantContext.setCurrentGymId(previousGymId);
        }
    }
}
