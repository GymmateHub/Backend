package com.gymmate.classes.application;

import com.gymmate.classes.domain.*;
import com.gymmate.classes.infrastructure.ClassBookingRepository;
import com.gymmate.classes.infrastructure.ClassScheduleRepository;
import com.gymmate.classes.infrastructure.GymClassRepository;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MemberMembershipRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassBookingService {

  private final ClassBookingRepository bookingRepository;
  private final ClassScheduleRepository scheduleRepository;
  private final GymClassRepository classRepository;
  private final MemberMembershipRepository membershipRepository;

  /**
   * Create a booking. If capacity reached, place on waitlist.
   */
  public ClassBooking createBooking(UUID gymId, UUID memberId, UUID scheduleId, String memberNotes) {
    // validate schedule
    ClassSchedule schedule = scheduleRepository.findById(scheduleId)
      .orElseThrow(() -> new ResourceNotFoundException("ClassSchedule", scheduleId.toString()));

    if (!schedule.isScheduled()) {
      throw new DomainException("SCHEDULE_NOT_AVAILABLE", "Class schedule is not open for booking");
    }

    // Validate that the schedule belongs to the specified gym
    if (!schedule.getGymId().equals(gymId)) {
      throw new DomainException("INVALID_GYM", "Class schedule does not belong to the specified gym");
    }

    // prevent duplicate booking
    if (bookingRepository.existsByScheduleIdAndMemberId(scheduleId, memberId)) {
      throw new DomainException("ALREADY_BOOKED", "Member already has a booking for this schedule");
    }

    // determine capacity
    Integer capacity = schedule.getCapacityOverride();
    if (capacity == null) {
      // fetch class and use its capacity
      UUID classId = schedule.getClassId();
      GymClass gymClass = classRepository.findById(classId)
        .orElseThrow(() -> new ResourceNotFoundException("GymClass", classId.toString()));
      capacity = gymClass.getCapacity();
    }

    long confirmedCount = bookingRepository.countConfirmedByScheduleId(scheduleId);
    boolean hasSpace = confirmedCount < (capacity == null ? Integer.MAX_VALUE : capacity);

    ClassBooking booking = ClassBooking.builder()
      .memberId(memberId)
      .classScheduleId(scheduleId)
      .memberNotes(memberNotes)
      .build();

    // Set the gymId to ensure proper tenant isolation
    booking.setGymId(gymId);

    if (hasSpace) {
      booking.setStatus(BookingStatus.CONFIRMED);

      // attempt to deduct membership credit if present
      membershipRepository.findActiveMembershipByMemberId(memberId).ifPresent(membership -> {
        Integer credits = membership.getClassCreditsRemaining();
        if (credits != null && credits > 0) {
          membership.setClassCreditsRemaining(credits - 1);
          membershipRepository.save(membership);
          booking.setCreditsUsed(1);
        }
      });

    } else {
      booking.setStatus(BookingStatus.WAITLISTED);
    }

    booking.setBookingDate(LocalDateTime.now());
    ClassBooking saved = bookingRepository.save(booking);
    log.info("Created booking {} for schedule {} member {} status={}", saved.getId(), scheduleId, memberId, saved.getStatus());
    return saved;
  }

  public ClassBooking getBooking(UUID bookingId) {
    return bookingRepository.findById(bookingId)
      .orElseThrow(() -> new ResourceNotFoundException("ClassBooking", bookingId.toString()));
  }

  public List<ClassBooking> getBookingsByMember(UUID memberId) {
    return bookingRepository.findByMemberId(memberId);
  }

  public List<ClassBooking> getBookingsBySchedule(UUID scheduleId) {
    return bookingRepository.findByScheduleId(scheduleId);
  }

  public List<ClassBooking> getWaitlist(UUID scheduleId) {
    return bookingRepository.findWaitlistByScheduleId(scheduleId);
  }

  /**
   * Cancel a booking. If a confirmed booking is cancelled, promote first waitlisted booking to confirmed.
   */
  public ClassBooking cancelBooking(UUID bookingId, String reason) {
    ClassBooking booking = getBooking(bookingId);

    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new DomainException("ALREADY_CANCELLED", "Booking already cancelled");
    }

    boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
    booking.cancel(reason);
    bookingRepository.save(booking);

    if (wasConfirmed) {
      // promote first waitlisted
      List<ClassBooking> waitlist = bookingRepository.findWaitlistByScheduleId(booking.getClassScheduleId());
      if (!waitlist.isEmpty()) {
        ClassBooking first = waitlist.getFirst();
        first.setStatus(BookingStatus.CONFIRMED);
        // consume membership credit if any
        membershipRepository.findActiveMembershipByMemberId(first.getMemberId()).ifPresent(membership -> {
          Integer credits = membership.getClassCreditsRemaining();
          if (credits != null && credits > 0) {
            membership.setClassCreditsRemaining(credits - 1);
            membershipRepository.save(membership);
            first.setCreditsUsed(1);
          }
        });
        bookingRepository.save(first);
        log.info("Promoted waitlist booking {} to confirmed", first.getId());
      }
    }

    return booking;
  }

  public ClassBooking checkIn(UUID bookingId) {
    ClassBooking booking = getBooking(bookingId);
    if (!booking.isConfirmed()) {
      throw new DomainException("NOT_CONFIRMED", "Only confirmed bookings can be checked in");
    }
    booking.checkIn();
    return bookingRepository.save(booking);
  }

  public ClassBooking checkOut(UUID bookingId) {
    ClassBooking booking = getBooking(bookingId);
    if (!booking.isCheckedIn()) {
      throw new DomainException("NOT_CHECKED_IN", "Member has not checked in");
    }
    booking.checkOut();
    return bookingRepository.save(booking);
  }
}
