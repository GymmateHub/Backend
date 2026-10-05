package com.gymmate.shared.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Domain entity owned by an organisation (tenant). When not set explicitly, the
 * persistence layer assigns the organisation of the current tenant context on insert.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TenantEntity extends BaseAuditEntity {

  private UUID organisationId;

  /**
   * Validates that this entity belongs to the specified organisation.
   * @throws IllegalStateException if organisationId doesn't match
   */
  public void validateTenant(UUID expectedOrganisationId) {
    if (organisationId != null && !organisationId.equals(expectedOrganisationId)) {
      throw new IllegalStateException("Entity belongs to a different organisation");
    }
  }
}
