package com.gymmate.classes.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for GymClass domain entity.
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
