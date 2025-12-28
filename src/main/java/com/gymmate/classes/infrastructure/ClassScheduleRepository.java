package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.ClassScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

  List<ClassSchedule> findByStatus(UUID gymId, ClassScheduleStatus status);

  boolean hasTrainerConflict(UUID trainerId, LocalDateTime startTime, LocalDateTime endTime);

  boolean hasAreaConflict(UUID areaId, LocalDateTime startTime, LocalDateTime endTime);

  void delete(ClassSchedule schedule);
}

