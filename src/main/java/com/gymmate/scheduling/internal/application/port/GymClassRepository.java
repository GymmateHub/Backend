package com.gymmate.scheduling.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.scheduling.internal.domain.GymClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for GymClass domain entity (moved to infrastructure).
 */
public interface GymClassRepository extends DomainRepository<GymClass, UUID> {

  List<GymClass> findByGymId(UUID gymId);

  List<GymClass> findByCategoryId(UUID categoryId);

  List<GymClass> findActiveByGymId(UUID gymId);

  Optional<GymClass> findByGymIdAndName(UUID gymId, String name);

  long countByGymId(UUID gymId);

  boolean existsByGymIdAndName(UUID gymId, String name);
}

