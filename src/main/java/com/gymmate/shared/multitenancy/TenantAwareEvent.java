package com.gymmate.shared.multitenancy;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface that must be implemented by all cross-module domain events.
 * Guarantees that every event published across module boundaries carries a durable
 * {@link TenantIdentity} to restore {@link TenantContext} in asynchronous listeners
 * and event publication outbox replays.
 */
public interface TenantAwareEvent {

    /**
     * Returns the tenant identity associated with this event. Derived from the event's own
     * tenant fields, so it is not part of the serialized payload.
     */
    @JsonIgnore
    TenantIdentity getTenantIdentity();
}
