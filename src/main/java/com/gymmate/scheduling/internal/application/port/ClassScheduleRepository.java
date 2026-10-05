package com.gymmate.scheduling.internal.application.port;

import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.constants.ClassScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository interface for ClassSchedule domain entity.
 */
public interface ClassScheduleRepository {

  ClassSchedule save(ClassSchedule schedule);

  Optional<ClassSchedule> findById(UUID id);

  List<ClassSchedule> findByGymId(UUID gymId);

  List<ClassSchedule> findByClassId(UUID classId);

  List<ClassSchedule> findByTrainerId(UUID trainerId);

  List<ClassSchedule> findByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end);

  List<ClassSchedule> findAvailableSchedules(UUID gymId, LocalDateTime start, LocalDateTime end);


  boolean hasTrainerConflict(UUID trainerId, LocalDateTime startTime, LocalDateTime endTime);

  boolean hasAreaConflict(UUID areaId, LocalDateTime startTime, LocalDateTime endTime);

  void delete(ClassSchedule schedule);
  
  List<ClassSchedule> findByGymIdAndStatus(UUID gymId, ClassScheduleStatus status);
  
  long countByGymIdAndStartTimeBetween(UUID gymId, LocalDateTime startTime, LocalDateTime endTime);
  
  int incrementBookedCountIfCapacityAvailable(UUID id, int capacity);
  
  int decrementBookedCount(UUID id);
  
  List<ClassSchedule> saveAll(Iterable<ClassSchedule> entities);
  
  boolean existsById(UUID id);
  
  List<ClassSchedule> findAll();
  
  List<ClassSchedule> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<ClassSchedule> entities);
  
  ClassSchedule saveAndFlush(ClassSchedule entity);
  
  void flush();
  
  Page<ClassSchedule> findAll(Pageable pageable);
}

