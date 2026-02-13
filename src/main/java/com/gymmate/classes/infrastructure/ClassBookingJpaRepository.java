package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.classes.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassBookingJpaRepository extends JpaRepository<ClassBooking, UUID> {
  List<ClassBooking> findByMemberId(UUID memberId);

  List<ClassBooking> findByClassScheduleId(UUID classScheduleId);

  @Query("SELECT cb FROM ClassBooking cb JOIN Member m ON cb.memberId = m.userId WHERE m.gymId = :gymId")
  List<ClassBooking> findByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT cb FROM ClassBooking cb JOIN Member m ON cb.memberId = m.userId WHERE m.gymId = :gymId AND cb.status = :status ORDER BY cb.bookingDate DESC")
  List<ClassBooking> findByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") BookingStatus status);

  Optional<ClassBooking> findByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);

  long countByClassScheduleId(UUID classScheduleId);

  @Query("SELECT COUNT(cb) FROM ClassBooking cb WHERE cb.classScheduleId = :scheduleId AND cb.status = 'CONFIRMED'")
  long countConfirmedByScheduleId(@Param("scheduleId") UUID scheduleId);

  @Query("SELECT cb FROM ClassBooking cb WHERE cb.classScheduleId = :scheduleId AND cb.status = 'WAITLISTED' ORDER BY cb.bookingDate")
  List<ClassBooking> findWaitlistByScheduleId(@Param("scheduleId") UUID scheduleId);

  @Query("SELECT cb FROM ClassBooking cb JOIN ClassSchedule cs ON cb.classScheduleId = cs.id WHERE cb.memberId = :memberId AND cs.startTime >= :fromDate AND cb.status IN ('CONFIRMED', 'WAITLISTED') ORDER BY cs.startTime")
  List<ClassBooking> findUpcomingByMemberId(@Param("memberId") UUID memberId,
      @Param("fromDate") LocalDateTime fromDate);

  boolean existsByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);

  // ===== Analytics Queries =====

  @Query("SELECT COUNT(cb) FROM ClassBooking cb JOIN Member m ON cb.memberId = m.userId WHERE m.gymId = :gymId AND cb.bookingDate BETWEEN :startDate AND :endDate")
  long countByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(cb) FROM ClassBooking cb JOIN Member m ON cb.memberId = m.userId WHERE m.gymId = :gymId AND cb.status = :status AND cb.bookingDate BETWEEN :startDate AND :endDate")
  long countByGymIdAndStatusAndDateRange(@Param("gymId") UUID gymId, @Param("status") BookingStatus status,
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT gc.name, COUNT(cb) FROM ClassBooking cb JOIN ClassSchedule cs ON cb.classScheduleId = cs.id JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cb.bookingDate BETWEEN :startDate AND :endDate GROUP BY gc.name ORDER BY COUNT(cb) DESC")
  List<Object[]> countBookingsByClassForGym(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT FUNCTION('DAYOFWEEK', cb.bookingDate), COUNT(cb) FROM ClassBooking cb JOIN Member m ON cb.memberId = m.userId WHERE m.gymId = :gymId AND cb.bookingDate BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DAYOFWEEK', cb.bookingDate)")
  List<Object[]> countBookingsByDayOfWeek(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT FUNCTION('HOUR', cs.startTime), COUNT(cb) FROM ClassBooking cb JOIN ClassSchedule cs ON cb.classScheduleId = cs.id JOIN GymClass gc ON cs.classId = gc.id JOIN ClassCategory cc ON gc.categoryId = cc.id WHERE cc.gymId = :gymId AND cb.bookingDate BETWEEN :startDate AND :endDate GROUP BY FUNCTION('HOUR', cs.startTime)")
  List<Object[]> countBookingsByTimeSlot(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
