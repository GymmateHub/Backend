package com.gymmate.classes.application;

import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.ClassScheduleStatus;
import com.gymmate.classes.infrastructure.ClassScheduleJpaRepository;
import com.gymmate.classes.infrastructure.ClassBookingJpaRepository;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassScheduleService {
  private final ClassScheduleJpaRepository scheduleRepository;
  private final ClassBookingJpaRepository bookingRepository;

  public ClassSchedule createSchedule(ClassSchedule schedule) {
    if (schedule.getGymId() == null) throw new DomainException("MISSING_GYM", "Gym id is required");
    // check trainer/area conflicts
    if (schedule.getTrainerId() != null && scheduleRepository.hasTrainerConflict(schedule.getTrainerId(), schedule.getStartTime(), schedule.getEndTime())) {
      throw new DomainException("TRAINER_CONFLICT", "Trainer has another scheduled class in this time range");
    }
    if (schedule.getAreaId() != null && scheduleRepository.hasAreaConflict(schedule.getAreaId(), schedule.getStartTime(), schedule.getEndTime())) {
      throw new DomainException("AREA_CONFLICT", "Area is occupied in this time range");
    }
    return scheduleRepository.save(schedule);
  }

  public ClassSchedule getSchedule(UUID id) {
    return scheduleRepository.findById(id).orElseThrow(() -> new DomainException("NOT_FOUND", "Class schedule not found"));
  }

  public List<ClassSchedule> listByGym(UUID gymId) {
    return scheduleRepository.findByGymId(gymId);
  }

  public List<ClassSchedule> listByClass(UUID classId) {
    return scheduleRepository.findByClassId(classId);
  }

  public ClassSchedule updateSchedule(ClassSchedule schedule) {
    ClassSchedule existing = getSchedule(schedule.getId());
    // only allow updates if not in progress/completed
    if (existing.getStatus() != ClassScheduleStatus.SCHEDULED) {
      throw new DomainException("INVALID_STATE", "Only scheduled classes can be updated");
    }
    existing.setStartTime(schedule.getStartTime());
    existing.setEndTime(schedule.getEndTime());
    existing.setCapacityOverride(schedule.getCapacityOverride());
    existing.setPriceOverride(schedule.getPriceOverride());
    existing.setTrainerId(schedule.getTrainerId());
    existing.setAreaId(schedule.getAreaId());
    return scheduleRepository.save(existing);
  }

  public void deleteSchedule(UUID id) {
    ClassSchedule existing = getSchedule(id);
    if (existing.getStatus() == ClassScheduleStatus.IN_PROGRESS) {
      throw new DomainException("INVALID_STATE", "Cannot delete an in-progress schedule");
    }
    scheduleRepository.delete(existing);
  }

  public List<ClassSchedule> findAvailable(UUID gymId, LocalDateTime start, LocalDateTime end) {
    return scheduleRepository.findAvailableSchedules(gymId, start, end);
  }
}
