package com.gymmate.membership.internal.application.port;

import com.gymmate.membership.internal.domain.MembershipPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository interface for MembershipPlan domain entity.
 * Following hexagonal architecture pattern.
 */
public interface MembershipPlanRepository {

  MembershipPlan save(MembershipPlan membershipPlan);

  Optional<MembershipPlan> findById(UUID id);

  List<MembershipPlan> findByGymId(UUID gymId);

  List<MembershipPlan> findActiveByGymId(UUID gymId);

  List<MembershipPlan> findFeaturedByGymId(UUID gymId);

  Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name);

  void delete(MembershipPlan membershipPlan);

  boolean existsByGymIdAndName(UUID gymId, String name);
  
  List<MembershipPlan> saveAll(Iterable<MembershipPlan> entities);
  
  boolean existsById(UUID id);
  
  List<MembershipPlan> findAll();
  
  List<MembershipPlan> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<MembershipPlan> entities);
  
  MembershipPlan saveAndFlush(MembershipPlan entity);
  
  void flush();
  
  Page<MembershipPlan> findAll(Pageable pageable);
}
