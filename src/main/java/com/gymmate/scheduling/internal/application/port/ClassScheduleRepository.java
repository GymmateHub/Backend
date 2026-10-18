package com.gymmate.scheduling.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.constants.ClassScheduleStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for ClassSchedule domain entity.
 */
public interface ClassScheduleRepository extends DomainRepository<ClassSchedule, UUID> {

  List<ClassSchedule> findByGymId(UUID gymId);

  List<ClassSchedule> findByClassId(UUID classId);

  List<ClassSchedule> findByTrainerId(UUID trainerId);

  List<ClassSchedule> findByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end);

  List<ClassSchedule> findAvailableSchedules(UUID gymId, LocalDateTime start, LocalDateTime end);

  boolean hasTrainerConflict(UUID trainerId, LocalDateTime startTime, LocalDateTime endTime);

  boolean hasAreaConflict(UUID areaId, LocalDateTime startTime, LocalDateTime endTime);

  List<ClassSchedule> findByGymIdAndStatus(UUID gymId, ClassScheduleStatus status);

  long countByGymIdAndStartTimeBetween(UUID gymId, LocalDateTime startTime, LocalDateTime endTime);

  int incrementBookedCountIfCapacityAvailable(UUID id, int capacity);

  int decrementBookedCount(UUID id);
}

