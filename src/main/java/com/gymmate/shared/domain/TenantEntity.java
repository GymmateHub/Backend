package com.gymmate.shared.domain;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Base entity for multi-tenant entities.
 * Extends BaseAuditEntity and adds organisationId for tenant isolation.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class TenantEntity extends BaseAuditEntity {

  @Column(name = "organisation_id", nullable = true)
  private UUID organisationId;

  @PrePersist
  protected void prePersist() {
    super.prePersist();

    if (organisationId == null) {
      UUID currentTenantId = TenantContext.getCurrentTenantId();
      if (currentTenantId != null) {
        organisationId = currentTenantId;
      }
    }
  }
}
