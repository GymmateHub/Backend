package com.gymmate.classes.internal.repository;

import com.gymmate.classes.internal.domain.GymClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for GymClass domain entity (moved to infrastructure).
 */
public interface GymClassRepository {

  GymClass save(GymClass gymClass);

  Optional<GymClass> findById(UUID id);

  List<GymClass> findByGymId(UUID gymId);

  List<GymClass> findByCategoryId(UUID categoryId);

  List<GymClass> findActiveByGymId(UUID gymId);

  Optional<GymClass> findByGymIdAndName(UUID gymId, String name);

  void delete(GymClass gymClass);

  long countByGymId(UUID gymId);

  boolean existsByGymIdAndName(UUID gymId, String name);
}

