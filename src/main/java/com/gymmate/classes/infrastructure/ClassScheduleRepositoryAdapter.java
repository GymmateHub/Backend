package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.ClassScheduleRepository;
import com.gymmate.classes.domain.ClassScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClassScheduleRepositoryAdapter implements ClassScheduleRepository {
  private final ClassScheduleJpaRepository jpaRepository;

  @Override
  public ClassSchedule save(ClassSchedule schedule) {
    return jpaRepository.save(schedule);
  }

  @Override
  public Optional<ClassSchedule> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<ClassSchedule> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<ClassSchedule> findByClassId(UUID classId) {
    return jpaRepository.findByClassId(classId);
  }

  @Override
  public List<ClassSchedule> findByTrainerId(UUID trainerId) {
    return jpaRepository.findByTrainerId(trainerId);
  }

  @Override
  public List<ClassSchedule> findByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end) {
    return jpaRepository.findByGymIdAndDateRange(gymId, start, end);
  }

  @Override
  public List<ClassSchedule> findAvailableSchedules(UUID gymId, LocalDateTime start, LocalDateTime end) {
    return jpaRepository.findAvailableSchedules(gymId, start, end);
  }

  @Override
  public List<ClassSchedule> findByStatus(UUID gymId, ClassScheduleStatus status) {
    return jpaRepository.findByGymIdAndStatus(gymId, status);
  }

  @Override
  public boolean hasTrainerConflict(UUID trainerId, LocalDateTime startTime, LocalDateTime endTime) {
    return jpaRepository.hasTrainerConflict(trainerId, startTime, endTime);
  }

  @Override
  public boolean hasAreaConflict(UUID areaId, LocalDateTime startTime, LocalDateTime endTime) {
    return jpaRepository.hasAreaConflict(areaId, startTime, endTime);
  }

  @Override
  public void delete(ClassSchedule schedule) {
    jpaRepository.delete(schedule);
  }
}
