package com.gymmate.scheduling.internal.application.port;

import com.gymmate.scheduling.internal.domain.GymArea;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface GymAreaRepository {

  GymArea save(GymArea area);

  Optional<GymArea> findById(UUID id);

  List<GymArea> findByGymId(UUID gymId);

  Optional<GymArea> findByGymIdAndName(UUID gymId, String name);

  void delete(GymArea area);

  boolean existsByGymIdAndName(UUID gymId, String name);
  
  List<GymArea> saveAll(Iterable<GymArea> entities);
  
  boolean existsById(UUID id);
  
  List<GymArea> findAll();
  
  List<GymArea> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<GymArea> entities);
  
  GymArea saveAndFlush(GymArea entity);
  
  void flush();
  
  Page<GymArea> findAll(Pageable pageable);

}

