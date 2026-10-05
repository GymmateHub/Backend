package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.AccessSchedule;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AccessScheduleRepository {

  List<AccessSchedule> findByMembershipPlanId(UUID membershipPlanId);
  
  AccessSchedule save(AccessSchedule entity);
  
  List<AccessSchedule> saveAll(Iterable<AccessSchedule> entities);
  
  Optional<AccessSchedule> findById(UUID id);
  
  boolean existsById(UUID id);
  
  List<AccessSchedule> findAll();
  
  List<AccessSchedule> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void delete(AccessSchedule entity);
  
  void deleteAll(Iterable<AccessSchedule> entities);
  
  AccessSchedule saveAndFlush(AccessSchedule entity);
  
  void flush();
  
  Page<AccessSchedule> findAll(Pageable pageable);
}
