package com.gymmate.shared.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Domain entity scoped to a gym within an organisation. When not set explicitly, the
 * persistence layer assigns the gym of the current tenant context on insert.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class GymScopedEntity extends TenantEntity {

  private UUID gymId;

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
