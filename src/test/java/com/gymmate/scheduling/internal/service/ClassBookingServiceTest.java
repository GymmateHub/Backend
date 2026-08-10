package com.gymmate.scheduling.internal.service;

import com.gymmate.scheduling.internal.domain.*;
import com.gymmate.scheduling.internal.repository.ClassBookingJpaRepository;
import com.gymmate.scheduling.internal.repository.ClassScheduleJpaRepository;
import com.gymmate.scheduling.internal.repository.GymClassJpaRepository;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassBookingServiceTest {

  private ClassBookingJpaRepository bookingRepository;
  private ClassScheduleJpaRepository scheduleRepository;
  private GymClassJpaRepository classRepository;
  private MemberMembershipRepository membershipRepository;
  private ApplicationEventPublisher eventPublisher;
  private ClassBookingService bookingService;

  @BeforeEach
  void setUp() {
    bookingRepository = mock(ClassBookingJpaRepository.class);
    scheduleRepository = mock(ClassScheduleJpaRepository.class);
    classRepository = mock(GymClassJpaRepository.class);
    membershipRepository = mock(MemberMembershipRepository.class);
    eventPublisher = mock(ApplicationEventPublisher.class);
    bookingService = new ClassBookingService(bookingRepository, scheduleRepository, classRepository, membershipRepository, eventPublisher);
  }

  @Test
  void createBooking_confirmedWhenSpaceAndDeductsCredit() {
    UUID gymId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    UUID scheduleId = UUID.randomUUID();
    UUID classId = UUID.randomUUID();

    ClassSchedule schedule = ClassSchedule.builder().classId(classId).startTime(LocalDateTime.now().plusDays(1)).endTime(LocalDateTime.now().plusDays(1).plusHours(1)).build();
    schedule.setId(scheduleId);
    schedule.setOrganisationId(gymId);
    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(bookingRepository.existsByClassScheduleIdAndMemberId(scheduleId, memberId)).thenReturn(false);

    GymClass gymClass = GymClass.builder().capacity(2).build();
    gymClass.setId(classId);
    when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
    when(scheduleRepository.incrementBookedCountIfCapacityAvailable(scheduleId, 2)).thenReturn(1);

    MemberMembership membership = MemberMembership.builder().classCreditsRemaining(3).build();
    membership.setId(UUID.randomUUID());
    when(membershipRepository.findActiveMembershipByMemberId(memberId)).thenReturn(Optional.of(membership));

    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));

    ClassBooking booking = bookingService.createBooking(gymId, memberId, scheduleId, "notes");

    assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    assertEquals(1, booking.getCreditsUsed());
    verify(membershipRepository).save(membership);
    verify(bookingRepository).save(any(ClassBooking.class));
  }

  @Test
  void createBooking_waitlistWhenFull() {
    UUID gymId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    UUID scheduleId = UUID.randomUUID();
    UUID classId = UUID.randomUUID();

    ClassSchedule schedule = ClassSchedule.builder().classId(classId).build();
    schedule.setId(scheduleId);
    schedule.setOrganisationId(gymId);
    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(bookingRepository.existsByClassScheduleIdAndMemberId(scheduleId, memberId)).thenReturn(false);

    GymClass gymClass = GymClass.builder().capacity(1).build();
    gymClass.setId(classId);
    when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
    // Schedule is already at capacity: the conditional update finds no row to update.
    when(scheduleRepository.incrementBookedCountIfCapacityAvailable(scheduleId, 1)).thenReturn(0);

    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));

    ClassBooking booking = bookingService.createBooking(gymId, memberId, scheduleId, null);

    assertEquals(BookingStatus.WAITLISTED, booking.getStatus());
    verify(bookingRepository).save(any(ClassBooking.class));
  }

  @Test
  void cancelBooking_promotesWaitlist() {
    UUID bookingId = UUID.randomUUID();
    UUID scheduleId = UUID.randomUUID();
    UUID waitlistBookingId = UUID.randomUUID();
    UUID classId = UUID.randomUUID();

    ClassBooking booking = ClassBooking.builder().classScheduleId(scheduleId).status(BookingStatus.CONFIRMED).build();
    booking.setId(bookingId);
    ClassBooking waitlisted = ClassBooking.builder().classScheduleId(scheduleId).memberId(UUID.randomUUID()).status(BookingStatus.WAITLISTED).build();
    waitlisted.setId(waitlistBookingId);

    ClassSchedule schedule = ClassSchedule.builder().classId(classId).build();
    schedule.setId(scheduleId);
    GymClass gymClass = GymClass.builder().capacity(1).build();
    gymClass.setId(classId);

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(bookingRepository.findWaitlistByScheduleId(scheduleId)).thenReturn(List.of(waitlisted));
    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));
    when(scheduleRepository.decrementBookedCount(scheduleId)).thenReturn(1);
    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
    // The seat freed by the cancellation is available again for the promoted booking.
    when(scheduleRepository.incrementBookedCountIfCapacityAvailable(scheduleId, 1)).thenReturn(1);

    ClassBooking cancelled = bookingService.cancelBooking(bookingId, "reason");

    assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    verify(bookingRepository, times(3)).save(any(ClassBooking.class)); // cancelled + promoted
    verify(scheduleRepository).decrementBookedCount(scheduleId);
    verify(scheduleRepository).incrementBookedCountIfCapacityAvailable(scheduleId, 1);
  }

  @Test
  void cancelBooking_waitlistStaysWaitlistedWhenSeatReclaimedElsewhere() {
    UUID bookingId = UUID.randomUUID();
    UUID scheduleId = UUID.randomUUID();
    UUID waitlistBookingId = UUID.randomUUID();
    UUID classId = UUID.randomUUID();

    ClassBooking booking = ClassBooking.builder().classScheduleId(scheduleId).status(BookingStatus.CONFIRMED).build();
    booking.setId(bookingId);
    ClassBooking waitlisted = ClassBooking.builder().classScheduleId(scheduleId).memberId(UUID.randomUUID()).status(BookingStatus.WAITLISTED).build();
    waitlisted.setId(waitlistBookingId);

    ClassSchedule schedule = ClassSchedule.builder().classId(classId).build();
    schedule.setId(scheduleId);
    GymClass gymClass = GymClass.builder().capacity(1).build();
    gymClass.setId(classId);

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(bookingRepository.findWaitlistByScheduleId(scheduleId)).thenReturn(List.of(waitlisted));
    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));
    when(scheduleRepository.decrementBookedCount(scheduleId)).thenReturn(1);
    when(scheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
    // A concurrent createBooking claimed the freed seat first.
    when(scheduleRepository.incrementBookedCountIfCapacityAvailable(scheduleId, 1)).thenReturn(0);

    ClassBooking cancelled = bookingService.cancelBooking(bookingId, "reason");

    assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    // cancelled booking + renumberWaitlist re-saving the still-waitlisted entry (at
    // position 1) — it is never promoted/saved as CONFIRMED since the seat wasn't
    // actually available.
    verify(bookingRepository, times(2)).save(any(ClassBooking.class));
    assertEquals(BookingStatus.WAITLISTED, waitlisted.getStatus());
  }

  @Test
  void checkIn_onlyConfirmed() {
    UUID bookingId = UUID.randomUUID();
    ClassBooking booking = ClassBooking.builder().status(BookingStatus.CONFIRMED).build();
    booking.setId(bookingId);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));

    ClassBooking checkedIn = bookingService.checkIn(bookingId);
    assertNotNull(checkedIn.getCheckedInAt());
    assertEquals(BookingStatus.CONFIRMED, checkedIn.getStatus());
  }

  @Test
  void checkOut_requiresCheckedIn() {
    UUID bookingId = UUID.randomUUID();
    ClassBooking booking = ClassBooking.builder().status(BookingStatus.CONFIRMED).build();
    booking.setId(bookingId);
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

    // not checked in
    assertThrows(DomainException.class, () -> bookingService.checkOut(bookingId));

    // simulate checked in
    booking.setCheckedInAt(LocalDateTime.now());
    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));

    ClassBooking out = bookingService.checkOut(bookingId);
    assertNotNull(out.getCheckedOutAt());
    assertEquals(BookingStatus.COMPLETED, out.getStatus());
  }
}
