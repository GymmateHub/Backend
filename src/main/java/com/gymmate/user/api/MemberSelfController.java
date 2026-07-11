package com.gymmate.user.api;

import com.gymmate.membership.api.dto.MemberMembershipResponse;
import com.gymmate.membership.application.MemberPaymentService;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.user.api.dto.*;
import com.gymmate.user.application.MemberSelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for member self-service — the API surface of the member
 * mobile app. All operations act on the authenticated member; the client
 * never passes member or gym IDs.
 */
@Slf4j
@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEMBER')")
@Tag(name = "Member Self-Service", description = "Member mobile app APIs")
public class MemberSelfController {

  private final MemberSelfService memberSelfService;

  @GetMapping
  @Operation(summary = "Get my profile")
  public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    MemberProfileResponse profile = memberSelfService.getProfile(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(profile));
  }

  @PatchMapping("/profile")
  @Operation(summary = "Update my profile")
  public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMyProfile(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody MemberSelfProfileUpdateRequest request) {
    MemberProfileResponse profile = memberSelfService.updateProfile(userDetails.getUserId(), request);
    return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
  }

  @GetMapping("/membership")
  @Operation(summary = "Get my active membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> getMyMembership(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    MemberSelfService.MemberMembershipResponseOrNull result =
        memberSelfService.getMyMembership(userDetails.getUserId());
    if (result.membership() == null) {
      return ResponseEntity.ok(ApiResponse.success(null, "No active membership"));
    }
    return ResponseEntity.ok(ApiResponse.success(result.membership()));
  }

  @GetMapping("/schedule")
  @Operation(summary = "Get upcoming class schedule for my gym")
  public ResponseEntity<ApiResponse<List<MemberScheduleItemResponse>>> getUpcomingSchedule(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @RequestParam(defaultValue = "14") int days) {
    List<MemberScheduleItemResponse> schedule =
        memberSelfService.getUpcomingSchedule(userDetails.getUserId(), days);
    return ResponseEntity.ok(ApiResponse.success(schedule));
  }

  @GetMapping("/bookings")
  @Operation(summary = "Get my bookings")
  public ResponseEntity<ApiResponse<List<MemberBookingResponse>>> getMyBookings(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    List<MemberBookingResponse> bookings = memberSelfService.getMyBookings(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(bookings));
  }

  @PostMapping("/bookings")
  @Operation(summary = "Book a class")
  public ResponseEntity<ApiResponse<MemberBookingResponse>> bookClass(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody SelfBookingRequest request) {
    MemberBookingResponse booking =
        memberSelfService.bookClass(userDetails.getUserId(), request.scheduleId(), request.notes());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(booking, "Class booked successfully"));
  }

  @PostMapping("/bookings/{bookingId}/cancel")
  @Operation(summary = "Cancel my booking")
  public ResponseEntity<ApiResponse<MemberBookingResponse>> cancelMyBooking(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @PathVariable UUID bookingId,
      @RequestParam(required = false) String reason) {
    MemberBookingResponse booking =
        memberSelfService.cancelMyBooking(userDetails.getUserId(), bookingId, reason);
    return ResponseEntity.ok(ApiResponse.success(booking, "Booking cancelled"));
  }

  @GetMapping("/payments")
  @Operation(summary = "Get my invoices")
  public ResponseEntity<ApiResponse<List<MemberPaymentService.MemberInvoiceResponse>>> getMyPayments(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    List<MemberPaymentService.MemberInvoiceResponse> invoices =
        memberSelfService.getMyInvoices(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(invoices));
  }

  @GetMapping("/progress")
  @Operation(summary = "Get my workout and health progress summary")
  public ResponseEntity<ApiResponse<MemberProgressResponse>> getMyProgress(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    MemberProgressResponse progress = memberSelfService.getMyProgress(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(progress));
  }

  @GetMapping("/qr-code")
  @Operation(summary = "Get my check-in QR credential")
  public ResponseEntity<ApiResponse<MemberQrCodeResponse>> getMyQrCode(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    MemberQrCodeResponse qrCode = memberSelfService.getMyQrCode(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(qrCode));
  }

  @GetMapping("/notifications")
  @Operation(summary = "Get my notification feed")
  public ResponseEntity<ApiResponse<List<MemberNotificationItem>>> getMyNotifications(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    List<MemberNotificationItem> notifications =
        memberSelfService.getMyNotifications(userDetails.getUserId());
    return ResponseEntity.ok(ApiResponse.success(notifications));
  }

  // ===== WORKOUTS =====

  @GetMapping("/workouts")
  @Operation(summary = "Get my workout history")
  public ResponseEntity<ApiResponse<List<MemberWorkoutResponse>>> getMyWorkouts(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    return ResponseEntity.ok(ApiResponse.success(memberSelfService.getMyWorkouts(userDetails.getUserId())));
  }

  @PostMapping("/workouts")
  @Operation(summary = "Log a workout")
  public ResponseEntity<ApiResponse<MemberWorkoutResponse>> logWorkout(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody LogWorkoutRequest request) {
    MemberWorkoutResponse workout = memberSelfService.logWorkout(userDetails.getUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(workout, "Workout logged"));
  }

  // ===== HEALTH METRICS =====

  @PostMapping("/health-metrics")
  @Operation(summary = "Record a health metric (weight, body fat, etc.)")
  public ResponseEntity<ApiResponse<Void>> recordMetric(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody RecordMetricRequest request) {
    memberSelfService.recordMetric(userDetails.getUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(null, "Measurement recorded"));
  }

  // ===== FITNESS GOALS =====

  @GetMapping("/goals")
  @Operation(summary = "Get my fitness goals")
  public ResponseEntity<ApiResponse<List<MemberGoalResponse>>> getMyGoals(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    return ResponseEntity.ok(ApiResponse.success(memberSelfService.getMyGoals(userDetails.getUserId())));
  }

  @PostMapping("/goals")
  @Operation(summary = "Create a fitness goal")
  public ResponseEntity<ApiResponse<MemberGoalResponse>> createGoal(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody CreateGoalRequest request) {
    MemberGoalResponse goal = memberSelfService.createGoal(userDetails.getUserId(), request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(goal, "Goal created"));
  }

  @PostMapping("/goals/{goalId}/progress")
  @Operation(summary = "Update my goal's progress")
  public ResponseEntity<ApiResponse<MemberGoalResponse>> updateGoalProgress(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @PathVariable UUID goalId,
      @RequestParam java.math.BigDecimal currentValue) {
    MemberGoalResponse goal =
        memberSelfService.updateGoalProgress(userDetails.getUserId(), goalId, currentValue);
    return ResponseEntity.ok(ApiResponse.success(goal, "Progress updated"));
  }

  @PostMapping("/goals/{goalId}/achieve")
  @Operation(summary = "Mark my goal as achieved")
  public ResponseEntity<ApiResponse<MemberGoalResponse>> achieveGoal(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @PathVariable UUID goalId) {
    MemberGoalResponse goal = memberSelfService.achieveGoal(userDetails.getUserId(), goalId);
    return ResponseEntity.ok(ApiResponse.success(goal, "Goal achieved — congratulations!"));
  }

  @PostMapping("/goals/{goalId}/abandon")
  @Operation(summary = "Abandon my goal")
  public ResponseEntity<ApiResponse<MemberGoalResponse>> abandonGoal(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @PathVariable UUID goalId,
      @RequestParam(required = false) String reason) {
    MemberGoalResponse goal = memberSelfService.abandonGoal(userDetails.getUserId(), goalId, reason);
    return ResponseEntity.ok(ApiResponse.success(goal, "Goal abandoned"));
  }

  // ===== MEMBERSHIP PLANS =====

  @GetMapping("/plans")
  @Operation(summary = "Get plans available at my gym")
  public ResponseEntity<ApiResponse<List<MemberPlanResponse>>> getAvailablePlans(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
    return ResponseEntity.ok(ApiResponse.success(memberSelfService.getAvailablePlans(userDetails.getUserId())));
  }

  @PostMapping("/subscribe")
  @Operation(summary = "Subscribe to a plan (payment settled with the gym)")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> subscribe(
      @AuthenticationPrincipal TenantAwareUserDetails userDetails,
      @Valid @RequestBody SubscribePlanRequest request) {
    MemberMembershipResponse membership =
        memberSelfService.subscribeToPlan(userDetails.getUserId(), request.planId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(membership, "Subscribed successfully"));
  }
}
