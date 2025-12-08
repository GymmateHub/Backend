package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ClassCategory domain entity (moved to infrastructure).
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

