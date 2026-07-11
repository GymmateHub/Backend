package com.gymmate.user.application;

import com.gymmate.classes.application.ClassBookingService;
import com.gymmate.classes.application.ClassScheduleService;
import com.gymmate.classes.application.GymClassService;
import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.GymClass;
import com.gymmate.health.application.FitnessGoalService;
import com.gymmate.health.application.HealthMetricService;
import com.gymmate.health.application.WorkoutTrackingService;
import com.gymmate.health.domain.Enums.GoalType;
import com.gymmate.health.domain.Enums.MetricType;
import com.gymmate.health.domain.Enums.WorkoutIntensity;
import com.gymmate.health.domain.FitnessGoal;
import com.gymmate.membership.application.MemberPaymentService;
import com.gymmate.membership.application.MembershipPlanService;
import com.gymmate.membership.application.MembershipService;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.api.dto.*;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Application service for member self-service use cases (the member mobile app).
 * Every operation derives the member from the authenticated user id, so a
 * member can only ever see or change their own data — IDs are never taken
 * from the client.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberSelfService {

  private final MemberService memberService;
  private final UserService userService;
  private final MembershipService membershipService;
  private final MembershipPlanService membershipPlanService;
  private final MemberPaymentService memberPaymentService;
  private final ClassScheduleService classScheduleService;
  private final GymClassService gymClassService;
  private final ClassBookingService classBookingService;
  private final WorkoutTrackingService workoutTrackingService;
  private final HealthMetricService healthMetricService;
  private final FitnessGoalService fitnessGoalService;
  private final TrainerService trainerService;

  /** QR check-in credentials are valid for 5 minutes after issue. */
  private static final int QR_VALIDITY_MINUTES = 5;

  private Member requireMember(UUID userId) {
    return memberService.findByUserId(userId);
  }

  public MemberProfileResponse getProfile(UUID userId) {
    Member member = requireMember(userId);
    User user = userService.findById(userId);
    return MemberProfileResponse.from(user, member);
  }

  @Transactional
  public MemberProfileResponse updateProfile(UUID userId, MemberSelfProfileUpdateRequest request) {
    Member member = requireMember(userId);
    User user = userService.findById(userId);

    String firstName = request.firstName() != null ? request.firstName() : user.getFirstName();
    String lastName = request.lastName() != null ? request.lastName() : user.getLastName();
    String phone = request.phone() != null ? request.phone() : user.getPhone();
    user = userService.updateProfile(userId, firstName, lastName, phone);

    if (request.emergencyContactName() != null || request.emergencyContactPhone() != null) {
      member = memberService.updateEmergencyContact(
          member.getId(),
          request.emergencyContactName() != null ? request.emergencyContactName() : member.getEmergencyContactName(),
          request.emergencyContactPhone() != null ? request.emergencyContactPhone() : member.getEmergencyContactPhone(),
          request.emergencyContactRelationship() != null
              ? request.emergencyContactRelationship()
              : member.getEmergencyContactRelationship());
    }

    return MemberProfileResponse.from(user, member);
  }

  /**
   * The member's active membership, or null when they have none.
   */
  public MemberMembershipResponseOrNull getMyMembership(UUID userId) {
    Member member = requireMember(userId);
    try {
      MemberMembership membership = membershipService.getActiveMembership(member.getId());
      return new MemberMembershipResponseOrNull(
          com.gymmate.membership.api.dto.MemberMembershipResponse.from(membership, member.getGymId()));
    } catch (ResourceNotFoundException | DomainException e) {
      return new MemberMembershipResponseOrNull(null);
    }
  }

  /** Wrapper so the controller can distinguish "no membership" from an error. */
  public record MemberMembershipResponseOrNull(
      com.gymmate.membership.api.dto.MemberMembershipResponse membership) {
  }

  /**
   * Upcoming class schedule for the member's gym within the next {@code days} days.
   */
  public List<MemberScheduleItemResponse> getUpcomingSchedule(UUID userId, int days) {
    Member member = requireMember(userId);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime until = now.plusDays(Math.max(1, Math.min(days, 60)));

    Map<UUID, GymClass> classesById = classesByIdForGym(member.getGymId());

    Map<UUID, String> trainerNames = new HashMap<>();
    return classScheduleService.listByGym(member.getGymId()).stream()
        .filter(s -> s.getStartTime() != null && s.getStartTime().isAfter(now) && s.getStartTime().isBefore(until))
        .sorted(Comparator.comparing(ClassSchedule::getStartTime))
        .map(s -> MemberScheduleItemResponse.from(
            s,
            classesById.get(s.getClassId()),
            confirmedBookingCount(s.getId()),
            trainerName(s.getTrainerId(), trainerNames)))
        .collect(Collectors.toList());
  }

  public List<MemberBookingResponse> getMyBookings(UUID userId) {
    Member member = requireMember(userId);
    Map<UUID, GymClass> classesById = classesByIdForGym(member.getGymId());

    return classBookingService.getBookingsByMember(member.getId()).stream()
        .map(b -> {
          ClassSchedule schedule = safeGetSchedule(b.getClassScheduleId());
          GymClass gymClass = schedule != null ? classesById.get(schedule.getClassId()) : null;
          return MemberBookingResponse.from(b, schedule, gymClass);
        })
        .sorted(Comparator.comparing(
            (MemberBookingResponse b) -> b.startTime() != null ? b.startTime() : LocalDateTime.MIN).reversed())
        .collect(Collectors.toList());
  }

  @Transactional
  public MemberBookingResponse bookClass(UUID userId, UUID scheduleId, String notes) {
    Member member = requireMember(userId);
    ClassBooking booking = classBookingService.createBooking(member.getGymId(), member.getId(), scheduleId, notes);
    ClassSchedule schedule = safeGetSchedule(scheduleId);
    GymClass gymClass = schedule != null ? safeGetClass(schedule.getClassId()) : null;
    log.info("Member {} booked schedule {}", member.getId(), scheduleId);
    return MemberBookingResponse.from(booking, schedule, gymClass);
  }

  @Transactional
  public MemberBookingResponse cancelMyBooking(UUID userId, UUID bookingId, String reason) {
    Member member = requireMember(userId);
    ClassBooking booking = classBookingService.getBooking(bookingId);
    if (!member.getId().equals(booking.getMemberId())) {
      // Respond as not-found so members can't probe other members' bookings
      throw new ResourceNotFoundException("Booking", bookingId.toString());
    }
    ClassBooking cancelled = classBookingService.cancelBooking(bookingId, reason);
    ClassSchedule schedule = safeGetSchedule(cancelled.getClassScheduleId());
    GymClass gymClass = schedule != null ? safeGetClass(schedule.getClassId()) : null;
    return MemberBookingResponse.from(cancelled, schedule, gymClass);
  }

  /**
   * The member's invoices. Returns an empty list when the member has no
   * billing history (e.g. no Stripe customer yet).
   */
  public List<MemberPaymentService.MemberInvoiceResponse> getMyInvoices(UUID userId) {
    Member member = requireMember(userId);
    try {
      return memberPaymentService.getMemberInvoices(member.getGymId(), member.getId());
    } catch (Exception e) {
      log.debug("No invoices available for member {}: {}", member.getId(), e.getMessage());
      return List.of();
    }
  }

  public MemberProgressResponse getMyProgress(UUID userId) {
    Member member = requireMember(userId);
    LocalDate today = LocalDate.now();

    WorkoutTrackingService.WorkoutStatistics stats =
        workoutTrackingService.calculateStatistics(member.getId(), today.minusDays(30), today);
    int streak = workoutTrackingService.calculateWorkoutStreak(member.getId());

    HealthMetricService.BodyCompositionSnapshot bodyComposition;
    try {
      bodyComposition = healthMetricService.getLatestBodyComposition(member.getId());
    } catch (Exception e) {
      bodyComposition = null;
    }

    return new MemberProgressResponse(stats, streak, bodyComposition);
  }

  public MemberQrCodeResponse getMyQrCode(UUID userId) {
    Member member = requireMember(userId);
    User user = userService.findById(userId);
    LocalDateTime now = LocalDateTime.now();
    return new MemberQrCodeResponse(
        member.getId(),
        member.getGymId(),
        member.getMembershipNumber(),
        user.getFirstName() + " " + user.getLastName(),
        member.getStatus() != null ? member.getStatus().name() : null,
        now,
        now.plusMinutes(QR_VALIDITY_MINUTES));
  }

  /**
   * Notification feed computed from the member's own data:
   * upcoming classes, waitlist positions, membership expiry/freeze, waiver.
   */
  public List<MemberNotificationItem> getMyNotifications(UUID userId) {
    Member member = requireMember(userId);
    List<MemberNotificationItem> items = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    Map<UUID, GymClass> classesById = classesByIdForGym(member.getGymId());
    for (ClassBooking booking : classBookingService.getBookingsByMember(member.getId())) {
      ClassSchedule schedule = safeGetSchedule(booking.getClassScheduleId());
      if (schedule == null || schedule.getStartTime() == null) continue;
      GymClass gymClass = classesById.get(schedule.getClassId());
      String className = gymClass != null ? gymClass.getName() : "Your class";

      boolean upcoming = schedule.getStartTime().isAfter(now)
          && schedule.getStartTime().isBefore(now.plusHours(48));
      String status = booking.getStatus() != null ? booking.getStatus().name() : "";

      if (upcoming && "WAITLISTED".equals(status)) {
        items.add(new MemberNotificationItem(
            "WAITLISTED",
            "You're on the waitlist",
            className + " on " + schedule.getStartTime().toLocalDate()
                + " — position " + booking.getWaitlistPosition(),
            schedule.getStartTime()));
      } else if (upcoming && "CONFIRMED".equals(status)) {
        items.add(new MemberNotificationItem(
            "UPCOMING_CLASS",
            "Upcoming class",
            className + " starts at " + schedule.getStartTime(),
            schedule.getStartTime()));
      }
    }

    try {
      MemberMembership membership = membershipService.getActiveMembership(member.getId());
      if (membership.getEndDate() != null) {
        long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), membership.getEndDate());
        if (daysLeft >= 0 && daysLeft <= 14) {
          items.add(new MemberNotificationItem(
              "MEMBERSHIP_EXPIRING",
              "Membership expiring soon",
              "Your membership ends in " + daysLeft + " day" + (daysLeft == 1 ? "" : "s") + ". Renew to keep training.",
              now));
        }
      }
      if (membership.isFrozen()) {
        items.add(new MemberNotificationItem(
            "MEMBERSHIP_FROZEN",
            "Membership frozen",
            membership.getFrozenUntil() != null
                ? "Your membership is frozen until " + membership.getFrozenUntil()
                : "Your membership is currently frozen.",
            now));
      }
    } catch (ResourceNotFoundException | DomainException ignored) {
      // No active membership — nothing to notify about
    }

    if (!member.isWaiverSigned()) {
      items.add(new MemberNotificationItem(
          "WAIVER_PENDING",
          "Waiver not signed",
          "Please sign your liability waiver at the front desk before your next visit.",
          now));
    }

    items.sort(Comparator.comparing(MemberNotificationItem::date));
    return items;
  }

  // ===== WORKOUTS =====

  public List<MemberWorkoutResponse> getMyWorkouts(UUID userId) {
    Member member = requireMember(userId);
    return workoutTrackingService.getWorkoutHistory(member.getId()).stream()
        .map(MemberWorkoutResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public MemberWorkoutResponse logWorkout(UUID userId, LogWorkoutRequest request) {
    Member member = requireMember(userId);
    WorkoutIntensity intensity = parseEnum(WorkoutIntensity.class, request.intensity(), WorkoutIntensity.MEDIUM);
    LocalDateTime workoutDate = request.workoutDate() != null ? request.workoutDate() : LocalDateTime.now();

    var workout = workoutTrackingService.logWorkout(
        member.getOrganisationId(),
        member.getGymId(),
        member.getId(),
        workoutDate,
        request.workoutName(),
        request.durationMinutes(),
        request.caloriesBurned(),
        intensity,
        request.notes(),
        List.of());
    log.info("Member {} logged workout ({} min)", member.getId(), request.durationMinutes());
    return MemberWorkoutResponse.from(workout);
  }

  // ===== HEALTH METRICS =====

  @Transactional
  public void recordMetric(UUID userId, RecordMetricRequest request) {
    Member member = requireMember(userId);
    MetricType metricType = parseEnum(MetricType.class, request.metricType(), null);
    if (metricType == null) {
      throw new DomainException("INVALID_METRIC_TYPE", "Unknown metric type: " + request.metricType());
    }
    healthMetricService.recordMetric(
        member.getOrganisationId(),
        member.getGymId(),
        member.getId(),
        metricType,
        request.value(),
        request.unit(),
        request.notes(),
        userId);
  }

  // ===== FITNESS GOALS =====

  public List<MemberGoalResponse> getMyGoals(UUID userId) {
    Member member = requireMember(userId);
    return fitnessGoalService.getMemberGoals(member.getId()).stream()
        .map(MemberGoalResponse::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public MemberGoalResponse createGoal(UUID userId, CreateGoalRequest request) {
    Member member = requireMember(userId);
    GoalType goalType = parseEnum(GoalType.class, request.goalType(), null);
    if (goalType == null) {
      throw new DomainException("INVALID_GOAL_TYPE", "Unknown goal type: " + request.goalType());
    }
    FitnessGoal goal = fitnessGoalService.createGoal(
        member.getOrganisationId(),
        member.getGymId(),
        member.getId(),
        goalType,
        request.title(),
        request.description(),
        request.targetValue(),
        request.targetUnit(),
        request.startValue(),
        LocalDate.now(),
        request.deadlineDate());
    return MemberGoalResponse.from(goal);
  }

  @Transactional
  public MemberGoalResponse updateGoalProgress(UUID userId, UUID goalId, java.math.BigDecimal currentValue) {
    requireOwnGoal(userId, goalId);
    return MemberGoalResponse.from(fitnessGoalService.updateGoalProgress(goalId, currentValue));
  }

  @Transactional
  public MemberGoalResponse achieveGoal(UUID userId, UUID goalId) {
    requireOwnGoal(userId, goalId);
    return MemberGoalResponse.from(fitnessGoalService.achieveGoal(goalId));
  }

  @Transactional
  public MemberGoalResponse abandonGoal(UUID userId, UUID goalId, String reason) {
    requireOwnGoal(userId, goalId);
    return MemberGoalResponse.from(fitnessGoalService.abandonGoal(goalId, reason));
  }

  // ===== MEMBERSHIP PLANS =====

  public List<MemberPlanResponse> getAvailablePlans(UUID userId) {
    Member member = requireMember(userId);
    return membershipPlanService.getActivePlansByGymId(member.getGymId()).stream()
        .map(MemberPlanResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * Subscribes the member to a plan starting today. Payment is settled
   * with the gym (front desk / invoice); in-app card payment is a
   * separate Stripe integration.
   */
  @Transactional
  public com.gymmate.membership.api.dto.MemberMembershipResponse subscribeToPlan(UUID userId, UUID planId) {
    Member member = requireMember(userId);
    MemberMembership membership =
        membershipService.subscribeMember(member.getGymId(), member.getId(), planId, LocalDate.now());
    log.info("Member {} subscribed to plan {}", member.getId(), planId);
    return com.gymmate.membership.api.dto.MemberMembershipResponse.from(membership, member.getGymId());
  }

  // ===== HELPER METHODS =====

  private void requireOwnGoal(UUID userId, UUID goalId) {
    Member member = requireMember(userId);
    FitnessGoal goal = fitnessGoalService.getGoalById(goalId);
    if (!member.getId().equals(goal.getMemberId())) {
      // Respond as not-found so members can't probe other members' goals
      throw new ResourceNotFoundException("FitnessGoal", goalId.toString());
    }
  }

  private <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
    if (value == null || value.isBlank()) return fallback;
    try {
      return Enum.valueOf(type, value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return fallback;
    }
  }

  private String trainerName(UUID trainerId, Map<UUID, String> cache) {
    if (trainerId == null) return null;
    return cache.computeIfAbsent(trainerId, id -> {
      try {
        var trainer = trainerService.findById(id);
        User user = userService.findById(trainer.getUserId());
        return user.getFirstName() + " " + user.getLastName();
      } catch (Exception e) {
        return null;
      }
    });
  }

  private int confirmedBookingCount(UUID scheduleId) {
    try {
      return (int) classBookingService.getBookingsBySchedule(scheduleId).stream()
          .filter(b -> b.getStatus() == com.gymmate.shared.constants.BookingStatus.CONFIRMED)
          .count();
    } catch (Exception e) {
      return 0;
    }
  }

  private Map<UUID, GymClass> classesByIdForGym(UUID gymId) {
    return gymClassService.listByGym(gymId).stream()
        .collect(Collectors.toMap(GymClass::getId, Function.identity(), (a, b) -> a));
  }

  private ClassSchedule safeGetSchedule(UUID scheduleId) {
    try {
      return classScheduleService.getSchedule(scheduleId);
    } catch (Exception e) {
      return null;
    }
  }

  private GymClass safeGetClass(UUID classId) {
    try {
      return gymClassService.getClass(classId);
    } catch (Exception e) {
      return null;
    }
  }
}
