package com.gymmate.classes.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ClassCategory domain entity.
 */
public interface ClassCategoryRepository {

  ClassCategory save(ClassCategory category);

  Optional<ClassCategory> findById(UUID id);

  List<ClassCategory> findByGymId(UUID gymId);

  List<ClassCategory> findActiveByGymId(UUID gymId);

  Optional<ClassCategory> findByGymIdAndName(UUID gymId, String name);

  void delete(ClassCategory category);

  boolean existsByGymIdAndName(UUID gymId, String name);
}
