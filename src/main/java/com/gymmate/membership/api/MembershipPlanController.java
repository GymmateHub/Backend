package com.gymmate.membership.api;

import com.gymmate.membership.api.dto.*;
import com.gymmate.membership.application.MembershipPlanService;
import com.gymmate.membership.domain.MembershipPlan;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for membership plan management.
 * Implements FR-005: Flexible Membership Types.
 */
@RestController
@RequestMapping("/api/membership-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Membership Plans", description = "Membership plan management APIs")
public class MembershipPlanController {

  private final MembershipPlanService membershipPlanService;

  @PostMapping
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'SUPER_ADMIN')")
  @Operation(summary = "Create membership plan", description = "Create a new membership plan for a gym")
  public ResponseEntity<ApiResponse<MembershipPlanResponse>> createPlan(
    @RequestParam UUID gymId,
    @Valid @RequestBody CreateMembershipPlanRequest request) {

    log.info("Creating membership plan for gym: {}", gymId);

    MembershipPlan plan = membershipPlanService.createPlan(
      gymId,
      request.name(),
      request.description(),
      request.price(),
      request.billingCycle(),
      request.durationMonths()
    );

    // Update additional features if provided
    if (request.classCredits() != null || request.guestPasses() != null || request.trainerSessions() != null) {
      plan = membershipPlanService.updatePlanFeatures(
        plan.getId(),
        request.classCredits(),
        request.guestPasses() != null ? request.guestPasses() : 0,
        request.trainerSessions() != null ? request.trainerSessions() : 0
      );
    }

    if (request.featured() != null && request.featured()) {
      plan = membershipPlanService.setFeatured(plan.getId(), true);
    }

    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success(MembershipPlanResponse.from(plan), "Membership plan created successfully"));
  }

  @GetMapping("/{planId}")
  @Operation(summary = "Get membership plan", description = "Get membership plan by ID")
  public ResponseEntity<ApiResponse<MembershipPlanResponse>> getPlan(@PathVariable UUID planId) {
    MembershipPlan plan = membershipPlanService.getPlanById(planId);
    return ResponseEntity.ok(ApiResponse.success(MembershipPlanResponse.from(plan)));
  }

  @GetMapping("/gym/{gymId}")
  @Operation(summary = "Get gym's membership plans", description = "Get all membership plans for a gym")
  public ResponseEntity<ApiResponse<List<MembershipPlanResponse>>> getGymPlans(
    @PathVariable UUID gymId,
    @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
    @RequestParam(required = false, defaultValue = "false") boolean featuredOnly) {

    List<MembershipPlan> plans;

    if (featuredOnly) {
      plans = membershipPlanService.getFeaturedPlansByGymId(gymId);
    } else if (activeOnly) {
      plans = membershipPlanService.getActivePlansByGymId(gymId);
    } else {
      plans = membershipPlanService.getPlansByGymId(gymId);
    }

    List<MembershipPlanResponse> responses = plans.stream()
      .map(MembershipPlanResponse::from)
      .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PutMapping("/{planId}/pricing")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'SUPER_ADMIN')")
  @Operation(summary = "Update plan pricing", description = "Update pricing for a membership plan")
  public ResponseEntity<ApiResponse<MembershipPlanResponse>> updatePricing(
    @PathVariable UUID planId,
    @RequestBody CreateMembershipPlanRequest request) {

    MembershipPlan plan = membershipPlanService.updatePlanPricing(
      planId,
      request.price(),
      request.billingCycle()
    );

    return ResponseEntity.ok(ApiResponse.success(MembershipPlanResponse.from(plan), "Plan pricing updated"));
  }

  @PutMapping("/{planId}/features")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'SUPER_ADMIN')")
  @Operation(summary = "Update plan features", description = "Update features for a membership plan")
  public ResponseEntity<ApiResponse<MembershipPlanResponse>> updateFeatures(
    @PathVariable UUID planId,
    @RequestBody CreateMembershipPlanRequest request) {

    MembershipPlan plan = membershipPlanService.updatePlanFeatures(
      planId,
      request.classCredits(),
      request.guestPasses() != null ? request.guestPasses() : 0,
      request.trainerSessions() != null ? request.trainerSessions() : 0
    );

    return ResponseEntity.ok(ApiResponse.success(MembershipPlanResponse.from(plan), "Plan features updated"));
  }

  @PutMapping("/{planId}/featured")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'SUPER_ADMIN')")
  @Operation(summary = "Set plan as featured", description = "Mark a plan as featured or not featured")
  public ResponseEntity<ApiResponse<MembershipPlanResponse>> setFeatured(
    @PathVariable UUID planId,
    @RequestParam boolean featured) {

    MembershipPlan plan = membershipPlanService.setFeatured(planId, featured);
    return ResponseEntity.ok(ApiResponse.success(MembershipPlanResponse.from(plan), "Featured status updated"));
  }

  @DeleteMapping("/{planId}")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'SUPER_ADMIN')")
  @Operation(summary = "Deactivate plan", description = "Deactivate a membership plan (soft delete)")
  public ResponseEntity<ApiResponse<Void>> deactivatePlan(@PathVariable UUID planId) {
    membershipPlanService.deactivatePlan(planId);
    return ResponseEntity.ok(ApiResponse.success(null, "Plan deactivated successfully"));
  }
}
