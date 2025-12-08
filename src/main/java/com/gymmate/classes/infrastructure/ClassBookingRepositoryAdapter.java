package com.gymmate.classes.infrastructure;

import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.classes.domain.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClassBookingRepositoryAdapter implements ClassBookingRepository {
  private final ClassBookingJpaRepository jpaRepository;

  @Override
  public ClassBooking save(ClassBooking booking) {
    return jpaRepository.save(booking);
  }

  @Override
  public Optional<ClassBooking> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<ClassBooking> findByMemberId(UUID memberId) {
    return jpaRepository.findByMemberId(memberId);
  }

  @Override
  public List<ClassBooking> findByScheduleId(UUID scheduleId) {
    return jpaRepository.findByClassScheduleId(scheduleId);
  }

  @Override
  public List<ClassBooking> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<ClassBooking> findByStatus(UUID gymId, BookingStatus status) {
    return jpaRepository.findByGymIdAndStatus(gymId, status);
  }

  @Override
  public Optional<ClassBooking> findByScheduleIdAndMemberId(UUID scheduleId, UUID memberId) {
    return jpaRepository.findByClassScheduleIdAndMemberId(scheduleId, memberId);
  }

  @Override
  public long countByScheduleId(UUID scheduleId) {
    return jpaRepository.countByClassScheduleId(scheduleId);
  }

  @Override
  public long countConfirmedByScheduleId(UUID scheduleId) {
    return jpaRepository.countConfirmedByScheduleId(scheduleId);
  }

  @Override
  public List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId) {
    return jpaRepository.findWaitlistByScheduleId(scheduleId);
  }

  @Override
  public List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate) {
    return jpaRepository.findUpcomingByMemberId(memberId, fromDate);
  }

  @Override
  public boolean existsByScheduleIdAndMemberId(UUID scheduleId, UUID memberId) {
    return jpaRepository.existsByClassScheduleIdAndMemberId(scheduleId, memberId);
  }

  @Override
  public void delete(ClassBooking booking) {
    jpaRepository.delete(booking);
  }
}
