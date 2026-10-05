package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.DoorBenefit;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface DoorBenefitRepository {

  boolean existsByAccessPointId(UUID accessPointId);

  boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId);
  
  DoorBenefit save(DoorBenefit entity);
  
  List<DoorBenefit> saveAll(Iterable<DoorBenefit> entities);
  
  Optional<DoorBenefit> findById(UUID id);
  
  boolean existsById(UUID id);
  
  List<DoorBenefit> findAll();
  
  List<DoorBenefit> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void delete(DoorBenefit entity);
  
  void deleteAll(Iterable<DoorBenefit> entities);
  
  DoorBenefit saveAndFlush(DoorBenefit entity);
  
  void flush();
  
  Page<DoorBenefit> findAll(Pageable pageable);
}
