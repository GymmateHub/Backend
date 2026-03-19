package com.gymmate.classes.application;

import com.gymmate.classes.domain.*;
import com.gymmate.classes.infrastructure.ClassBookingJpaRepository;
import com.gymmate.classes.infrastructure.ClassScheduleJpaRepository;
import com.gymmate.classes.infrastructure.GymClassJpaRepository;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.notification.events.WaitlistPromotedEvent;
import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassBookingService {

  private final ClassBookingJpaRepository bookingRepository;
  private final ClassScheduleJpaRepository scheduleRepository;
  private final GymClassJpaRepository classRepository;
  private final MemberMembershipRepository membershipRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Create a booking. If capacity reached, place on waitlist with position tracking.
   */
  public ClassBooking createBooking(UUID gymId, UUID memberId, UUID scheduleId, String memberNotes) {
    // validate schedule
    ClassSchedule schedule = scheduleRepository.findById(scheduleId)
      .orElseThrow(() -> new ResourceNotFoundException("ClassSchedule", scheduleId.toString()));

    if (!schedule.isScheduled()) {
      throw new DomainException("SCHEDULE_NOT_AVAILABLE", "Class schedule is not open for booking");
    }

    // prevent duplicate booking
    if (bookingRepository.existsByClassScheduleIdAndMemberId(scheduleId, memberId)) {
      throw new DomainException("ALREADY_BOOKED", "Member already has a booking for this schedule");
    }

    // determine capacity
    Integer capacity = schedule.getCapacityOverride();
    if (capacity == null) {
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
      // Calculate waitlist position: count existing waitlisted bookings + 1
      List<ClassBooking> existingWaitlist = bookingRepository.findWaitlistByScheduleId(scheduleId);
      booking.setWaitlistPosition(existingWaitlist.size() + 1);
    }

    booking.setBookingDate(LocalDateTime.now());
    ClassBooking saved = bookingRepository.save(booking);
    log.info("Created booking {} for schedule {} member {} status={} waitlistPos={}",
      saved.getId(), scheduleId, memberId, saved.getStatus(), saved.getWaitlistPosition());
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
    return bookingRepository.findByClassScheduleId(scheduleId);
  }

  public List<ClassBooking> getWaitlist(UUID scheduleId) {
    return bookingRepository.findWaitlistByScheduleId(scheduleId);
  }

  /**
   * Cancel a booking. If a confirmed booking is cancelled, promote first waitlisted
   * booking to confirmed and re-number remaining waitlist positions.
   */
  public ClassBooking cancelBooking(UUID bookingId, String reason) {
    ClassBooking booking = getBooking(bookingId);

    if (booking.getStatus() == BookingStatus.CANCELLED) {
      throw new DomainException("ALREADY_CANCELLED", "Booking already cancelled");
    }

    boolean wasConfirmed = booking.getStatus() == BookingStatus.CONFIRMED;
    boolean wasWaitlisted = booking.getStatus() == BookingStatus.WAITLISTED;
    booking.cancel(reason);
    booking.setWaitlistPosition(null); // Clear waitlist position on cancel
    bookingRepository.save(booking);

    if (wasConfirmed) {
      // promote first waitlisted
      List<ClassBooking> waitlist = bookingRepository.findWaitlistByScheduleId(booking.getClassScheduleId());
      if (!waitlist.isEmpty()) {
        ClassBooking first = waitlist.get(0);
        first.setStatus(BookingStatus.CONFIRMED);
        first.setWaitlistPosition(null); // No longer on waitlist
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

        // Notify member they've been promoted from waitlist
        try {
          eventPublisher.publishEvent(WaitlistPromotedEvent.builder()
              .organisationId(first.getOrganisationId())
              .gymId(first.getGymId())
              .memberId(first.getMemberId())
              .bookingId(first.getId())
              .scheduleId(first.getClassScheduleId())
              .build());
        } catch (Exception e) {
          log.warn("Failed to publish WaitlistPromotedEvent for booking {}: {}", first.getId(), e.getMessage());
        }

        // Re-number remaining waitlist positions
        renumberWaitlist(booking.getClassScheduleId());
      }
    } else if (wasWaitlisted) {
      // Re-number waitlist after a waitlisted booking is removed
      renumberWaitlist(booking.getClassScheduleId());
    }

    return booking;
  }

  /**
   * Re-number waitlist positions sequentially starting from 1.
   */
  private void renumberWaitlist(UUID scheduleId) {
    List<ClassBooking> waitlist = bookingRepository.findWaitlistByScheduleId(scheduleId);
    for (int i = 0; i < waitlist.size(); i++) {
      ClassBooking wb = waitlist.get(i);
      wb.setWaitlistPosition(i + 1);
      bookingRepository.save(wb);
    }
    log.debug("Re-numbered {} waitlist entries for schedule {}", waitlist.size(), scheduleId);
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
