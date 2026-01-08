package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.FreezePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FreezePolicyRepositoryAdapter implements FreezePolicyRepository {

  private final FreezePolicyJpaRepository jpaRepository;

  @Override
  public FreezePolicy save(FreezePolicy policy) {
    return jpaRepository.save(policy);
  }

  @Override
  public Optional<FreezePolicy> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Optional<FreezePolicy> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public Optional<FreezePolicy> findDefaultPolicy() {
    return jpaRepository.findDefaultPolicy();
  }

  @Override
  public Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId) {
    return jpaRepository.findDefaultPolicyByOrganisation(organisationId);
  }

  @Override
  public void delete(FreezePolicy policy) {
    jpaRepository.delete(policy);
  }
}
