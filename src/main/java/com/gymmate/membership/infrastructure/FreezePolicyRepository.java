package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.FreezePolicy;

import java.util.Optional;
import java.util.UUID;

public interface FreezePolicyRepository {
  FreezePolicy save(FreezePolicy policy);
  Optional<FreezePolicy> findById(UUID id);
  Optional<FreezePolicy> findActiveByGymId(UUID gymId);
  Optional<FreezePolicy> findDefaultPolicy();
  Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId);
  void delete(FreezePolicy policy);
}
