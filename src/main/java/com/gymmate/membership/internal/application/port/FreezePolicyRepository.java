package com.gymmate.membership.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.membership.internal.domain.FreezePolicy;

import java.util.Optional;
import java.util.UUID;

public interface FreezePolicyRepository extends DomainRepository<FreezePolicy, UUID> {
  Optional<FreezePolicy> findActiveByGymId(UUID gymId);
  Optional<FreezePolicy> findDefaultPolicy();
  Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId);

  Optional<FreezePolicy> findByGymIdAndActiveTrue(UUID gymId);

  Optional<FreezePolicy> findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(UUID organisationId);
}
