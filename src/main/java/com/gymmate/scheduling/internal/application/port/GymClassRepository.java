package com.gymmate.scheduling.internal.application.port;

import com.gymmate.scheduling.internal.domain.GymClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<GymClass> saveAll(Iterable<GymClass> entities);
  
  boolean existsById(UUID id);
  
  List<GymClass> findAll();
  
  List<GymClass> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<GymClass> entities);
  
  GymClass saveAndFlush(GymClass entity);
  
  void flush();
  
  Page<GymClass> findAll(Pageable pageable);
}

