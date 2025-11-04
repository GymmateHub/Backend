package com.gymmate.shared.domain;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Base entity for multi-tenant entities.
 * Extends BaseAuditEntity and adds gymId for tenant isolation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class TenantEntity extends BaseAuditEntity {

  @Column(name = "gym_id", nullable = true)
  private UUID gymId;

  @Override
  protected void prePersist() {
    super.prePersist();

    if (gymId == null) {
      UUID currentTenantId = TenantContext.getCurrentTenantId();
      if (currentTenantId != null) {
        gymId = currentTenantId;
      }
    }
  }
}
