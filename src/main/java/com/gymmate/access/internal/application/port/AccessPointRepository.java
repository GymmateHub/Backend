package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.AccessPoint;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AccessPointRepository {

  List<AccessPoint> findByGymId(UUID gymId);

  List<AccessPoint> findByOrganisationId(UUID organisationId);
  
  AccessPoint save(AccessPoint entity);
  
  List<AccessPoint> saveAll(Iterable<AccessPoint> entities);
  
  Optional<AccessPoint> findById(UUID id);
  
  boolean existsById(UUID id);
  
  List<AccessPoint> findAll();
  
  List<AccessPoint> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void delete(AccessPoint entity);
  
  void deleteAll(Iterable<AccessPoint> entities);
  
  AccessPoint saveAndFlush(AccessPoint entity);
  
  void flush();
  
  Page<AccessPoint> findAll(Pageable pageable);
}
