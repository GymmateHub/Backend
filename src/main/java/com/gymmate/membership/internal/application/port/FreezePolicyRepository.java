package com.gymmate.membership.internal.application.port;

import com.gymmate.membership.internal.domain.FreezePolicy;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface FreezePolicyRepository {
  FreezePolicy save(FreezePolicy policy);
  Optional<FreezePolicy> findById(UUID id);
  Optional<FreezePolicy> findActiveByGymId(UUID gymId);
  Optional<FreezePolicy> findDefaultPolicy();
  Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId);
  void delete(FreezePolicy policy);
  
  Optional<FreezePolicy> findByGymIdAndActiveTrue(UUID gymId);
  
  Optional<FreezePolicy> findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(UUID organisationId);
  
  List<FreezePolicy> saveAll(Iterable<FreezePolicy> entities);
  
  boolean existsById(UUID id);
  
  List<FreezePolicy> findAll();
  
  List<FreezePolicy> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<FreezePolicy> entities);
  
  FreezePolicy saveAndFlush(FreezePolicy entity);
  
  void flush();
  
  Page<FreezePolicy> findAll(Pageable pageable);
}
