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
  List<ClassBooking> findUpcomingByMemberId(@Param("memberId") UUID memberId, @Param("fromDate") LocalDateTime fromDate);

  boolean existsByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);
}
