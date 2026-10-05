package com.gymmate.scheduling.internal.application.port;

import com.gymmate.scheduling.internal.domain.ClassCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<ClassCategory> saveAll(Iterable<ClassCategory> entities);
  
  boolean existsById(UUID id);
  
  List<ClassCategory> findAll();
  
  List<ClassCategory> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<ClassCategory> entities);
  
  ClassCategory saveAndFlush(ClassCategory entity);
  
  void flush();
  
  Page<ClassCategory> findAll(Pageable pageable);
}

