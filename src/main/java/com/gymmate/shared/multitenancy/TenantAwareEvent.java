package com.gymmate.shared.multitenancy;

/**
 * Interface that must be implemented by all cross-module domain events.
 * Guarantees that every event published across module boundaries carries a durable
 * {@link TenantIdentity} to restore {@link TenantContext} in asynchronous listeners
 * and event publication outbox replays.
 */
public interface TenantAwareEvent {

    /**
     * Returns the tenant identity associated with this event.
     */
    TenantIdentity getTenantIdentity();
}
