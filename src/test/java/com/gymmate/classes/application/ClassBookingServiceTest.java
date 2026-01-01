package com.gymmate.classes.application;

import com.gymmate.classes.domain.*;
import com.gymmate.classes.infrastructure.ClassBookingJpaRepository;
import com.gymmate.classes.infrastructure.ClassScheduleJpaRepository;
import com.gymmate.classes.infrastructure.GymClassJpaRepository;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  private ClassBookingService bookingService;

  @BeforeEach
  void setUp() {
    bookingRepository = mock(ClassBookingJpaRepository.class);
    scheduleRepository = mock(ClassScheduleJpaRepository.class);
    classRepository = mock(GymClassJpaRepository.class);
    membershipRepository = mock(MemberMembershipRepository.class);
    bookingService = new ClassBookingService(bookingRepository, scheduleRepository, classRepository, membershipRepository);
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
    when(bookingRepository.countConfirmedByScheduleId(scheduleId)).thenReturn(0L);

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
    when(bookingRepository.countConfirmedByScheduleId(scheduleId)).thenReturn(1L);

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

    ClassBooking booking = ClassBooking.builder().classScheduleId(scheduleId).status(BookingStatus.CONFIRMED).build();
    booking.setId(bookingId);
    ClassBooking waitlisted = ClassBooking.builder().classScheduleId(scheduleId).memberId(UUID.randomUUID()).status(BookingStatus.WAITLISTED).build();
    waitlisted.setId(waitlistBookingId);

    when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
    when(bookingRepository.findWaitlistByScheduleId(scheduleId)).thenReturn(List.of(waitlisted));
    when(bookingRepository.save(any(ClassBooking.class))).thenAnswer(inv -> inv.getArgument(0));

    ClassBooking cancelled = bookingService.cancelBooking(bookingId, "reason");

    assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    verify(bookingRepository, times(2)).save(any(ClassBooking.class)); // cancelled + promoted
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
