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
 * Base entity for gym-scoped multi-tenant entities.
 * Extends TenantEntity and adds gymId for gym-level isolation.
 *
 * All entities extending this class will automatically:
 * 1. Have organisationId set from TenantContext on persist (inherited)
 * 2. Have gymId set from TenantContext on persist
 * 3. Be filtered by both organisationId AND gymId when filters are enabled
 *
 * Use this for gym-scoped entities: Member, GymClass, ClassSchedule,
 * ClassBooking, MemberMembership, MemberInvoice, etc.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@FilterDef(
    name = "gymFilter",
    parameters = @ParamDef(name = "gymId", type = UUID.class),
    defaultCondition = "gym_id = :gymId"
)
@Filter(name = "tenantFilter")
@Filter(name = "gymFilter")
public abstract class GymScopedEntity extends TenantEntity {

  @Column(name = "gym_id")
  @Getter
  @Setter
  private UUID gymId;

  @Override
  protected void prePersist() {
    super.prePersist();

    if (gymId == null) {
      UUID currentGymId = TenantContext.getCurrentGymId();
      if (currentGymId != null) {
        gymId = currentGymId;
      }
    }
  }

  /**
   * Validates that this entity belongs to the specified gym.
   * @throws IllegalStateException if gymId doesn't match
   */
  public void validateGym(UUID expectedGymId) {
    if (gymId != null && !gymId.equals(expectedGymId)) {
      throw new IllegalStateException("Entity belongs to a different gym");
    }
  }

  /**
   * Validates that this entity belongs to the specified organisation and gym.
   * @throws IllegalStateException if either doesn't match
   */
  public void validateTenantAndGym(UUID expectedOrganisationId, UUID expectedGymId) {
    validateTenant(expectedOrganisationId);
    validateGym(expectedGymId);
  }
}

