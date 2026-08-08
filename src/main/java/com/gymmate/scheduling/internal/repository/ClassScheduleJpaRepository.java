package com.gymmate.scheduling.internal.repository;

import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.constants.ClassScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassScheduleJpaRepository extends JpaRepository<ClassSchedule, UUID> {

  @Query("SELECT cs FROM ClassSchedule cs JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId")
  List<ClassSchedule> findByGymId(@Param("gymId") UUID gymId);

  List<ClassSchedule> findByClassId(UUID classId);

  List<ClassSchedule> findByTrainerId(UUID trainerId);

  @Query("SELECT cs FROM ClassSchedule cs JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cs.startTime >= :start AND cs.endTime <= :end ORDER BY cs.startTime")
  List<ClassSchedule> findByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT cs FROM ClassSchedule cs JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cs.startTime >= :start AND cs.endTime <= :end AND cs.status = 'SCHEDULED' ORDER BY cs.startTime")
  List<ClassSchedule> findAvailableSchedules(@Param("gymId") UUID gymId, @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT cs FROM ClassSchedule cs JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cs.status = :status ORDER BY cs.startTime")
  List<ClassSchedule> findByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") ClassScheduleStatus status);

  @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs WHERE cs.trainerId = :trainerId AND cs.status = 'SCHEDULED' AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
  boolean hasTrainerConflict(@Param("trainerId") UUID trainerId, @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query("SELECT COUNT(cs) > 0 FROM ClassSchedule cs WHERE cs.areaId = :areaId AND cs.status = 'SCHEDULED' AND ((cs.startTime < :endTime AND cs.endTime > :startTime))")
  boolean hasAreaConflict(@Param("areaId") UUID areaId, @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query("SELECT COUNT(cs) FROM ClassSchedule cs JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cs.startTime BETWEEN :startTime AND :endTime")
  long countByGymIdAndStartTimeBetween(@Param("gymId") UUID gymId, @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  /**
   * Atomically claims one seat: increments bookedCount only if it is still below the
   * given (already-resolved) effective capacity. Returns the number of rows updated —
   * 0 means the seat was not available (caller should waitlist instead of confirming).
   * The DB trigger (V13 migration) is the backstop if this invariant is ever bypassed.
   */
  @Modifying
  @Query("UPDATE ClassSchedule cs SET cs.bookedCount = cs.bookedCount + 1 WHERE cs.id = :id AND cs.bookedCount < :capacity")
  int incrementBookedCountIfCapacityAvailable(@Param("id") UUID id, @Param("capacity") int capacity);

  /**
   * Releases one seat (cancellation / waitlist removal), floored at zero.
   */
  @Modifying
  @Query("UPDATE ClassSchedule cs SET cs.bookedCount = CASE WHEN cs.bookedCount > 0 THEN cs.bookedCount - 1 ELSE 0 END WHERE cs.id = :id")
  int decrementBookedCount(@Param("id") UUID id);
}
