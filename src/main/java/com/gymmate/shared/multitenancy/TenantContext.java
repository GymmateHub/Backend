package com.gymmate.shared.multitenancy;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public static void setCurrentTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Return the current tenant id for this thread or null if not set.
     */
    public static UUID getCurrentTenantId() {
        return currentTenant.get();
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

    public static void clear() {
        currentTenant.remove();
    }
}
