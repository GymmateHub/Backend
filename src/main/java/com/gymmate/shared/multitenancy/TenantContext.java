package com.gymmate.shared.multitenancy;

import java.util.UUID;

public class TenantContext {
  private static final ThreadLocal<UUID> currentTenant = new InheritableThreadLocal<>();

  public static void setCurrentTenantId(UUID tenantId) {
    currentTenant.set(tenantId);
  }

  public static UUID getCurrentTenantId() {
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
