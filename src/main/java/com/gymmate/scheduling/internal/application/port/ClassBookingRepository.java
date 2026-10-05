package com.gymmate.scheduling.internal.application.port;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.scheduling.internal.domain.ClassBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface ClassBookingRepository {

  void delete(ClassBooking booking);


  List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate);

  List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId);

  long countConfirmedByScheduleId(UUID scheduleId);




  List<ClassBooking> findByGymId(UUID gymId);


  List<ClassBooking> findByMemberId(UUID memberId);

  Optional<ClassBooking> findById(UUID id);

  ClassBooking save(ClassBooking booking);
  
  List<ClassBooking> findByClassScheduleId(UUID classScheduleId);
  
  List<ClassBooking> findByGymIdAndStatus(UUID gymId, BookingStatus status);
  
  Optional<ClassBooking> findByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);
  
  long countByClassScheduleId(UUID classScheduleId);
  
  boolean existsByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);
  
  long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
  
  long countByGymIdAndStatusAndDateRange(UUID gymId, BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);
  
  List<Object[]> countBookingsByClassForGym(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
  
  List<Object[]> countBookingsByDayOfWeek(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
  
  List<Object[]> countBookingsByTimeSlot(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
  
  List<ClassBooking> saveAll(Iterable<ClassBooking> entities);
  
  boolean existsById(UUID id);
  
  List<ClassBooking> findAll();
  
  List<ClassBooking> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<ClassBooking> entities);
  
  ClassBooking saveAndFlush(ClassBooking entity);
  
  void flush();
  
  Page<ClassBooking> findAll(Pageable pageable);
}


