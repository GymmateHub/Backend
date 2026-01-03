package com.gymmate.shared.domain;

import com.gymmate.shared.multitenancy.TenantContext;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Base entity for multi-tenant entities scoped to an Organisation.
 * Extends BaseAuditEntity and adds organisationId for tenant isolation.
 *
 * All entities extending this class will automatically:
 * 1. Have organisationId set from TenantContext on persist
 * 2. Be filtered by organisationId when the tenantFilter is enabled
 *
 * Use this for org-level entities: User, Gym, Subscription, etc.
 * For gym-scoped entities (Members, Classes), use GymScopedEntity instead.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@FilterDef(
    name = "tenantFilter",
    parameters = @ParamDef(name = "organisationId", type = UUID.class),
    defaultCondition = "organisation_id = :organisationId"
)
@Filter(name = "tenantFilter")
public abstract class TenantEntity extends BaseAuditEntity {

  @Column(name = "organisation_id")
  @Getter
  @Setter
  private UUID organisationId;

  @Override
  protected void prePersist() {
    super.prePersist();

    if (organisationId == null) {
      UUID currentTenantId = TenantContext.getCurrentTenantId();
      if (currentTenantId != null) {
        organisationId = currentTenantId;
      }
    }
  }

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
