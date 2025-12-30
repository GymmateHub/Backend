package com.gymmate.shared.multitenancy;

import java.util.UUID;

/**
 * Thread-local context for multi-tenant isolation.
 * Stores both organisation (tenant) ID and current gym ID.
 */
public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentGym = new ThreadLocal<>();

    /**
     * Set the current tenant (organisation) ID for this thread.
     */
    public static void setCurrentTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Set the current gym ID for this thread (for gym-specific operations).
     */
    public static void setCurrentGymId(UUID gymId) {
        currentGym.set(gymId);
    }

    /**
     * Return the current tenant (organisation) id for this thread or null if not set.
     */
    public static UUID getCurrentTenantId() {
        return currentTenant.get();
    }

    /**
     * Return the current gym id for this thread or null if not set.
     */
    public static UUID getCurrentGymId() {
        return currentGym.get();
    }

    /**
     * Return the current tenant id or throw if not set.
     */
    public static UUID requireCurrentTenantId() {
        UUID tenantId = currentTenant.get();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context set for current thread");
        }
        return tenantId;
    }

    /**
     * Return the current gym id or throw if not set.
     */
    public static UUID requireCurrentGymId() {
        UUID gymId = currentGym.get();
        if (gymId == null) {
            throw new IllegalStateException("No gym context set for current thread");
        }
        return gymId;
    }

    /**
     * Clear both tenant and gym context.
     */
    public static void clear() {
        currentTenant.remove();
        currentGym.remove();
    }
}
