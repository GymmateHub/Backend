package com.gymmate.membership.api;

import com.gymmate.membership.api.dto.*;
import com.gymmate.membership.application.MembershipService;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for member membership management.
 * Implements FR-006: Membership Lifecycle, FR-007: Automated Billing.
 */
@RestController
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Memberships", description = "Member membership management APIs")
public class MembershipController {

  private final MembershipService membershipService;

  @PostMapping("/subscribe")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'MEMBER', 'SUPER_ADMIN')")
  @Operation(summary = "Subscribe member", description = "Subscribe a member to a membership plan")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> subscribeMember(
    @RequestParam UUID gymId,
    @Valid @RequestBody SubscribeMemberRequest request) {

    log.info("Subscribing member {} to plan {}", request.memberId(), request.planId());

    LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now();

    MemberMembership membership = membershipService.subscribeMember(
      gymId,
      request.memberId(),
      request.planId(),
      startDate
    );

    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success(MemberMembershipResponse.from(membership), "Member subscribed successfully"));
  }

  @GetMapping("/{membershipId}")
  @Operation(summary = "Get membership", description = "Get membership by ID")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> getMembership(@PathVariable UUID membershipId) {
    MemberMembership membership = membershipService.getMembershipById(membershipId);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership)));
  }

  @GetMapping("/member/{memberId}")
  @Operation(summary = "Get member's memberships", description = "Get membership history for a member")
  public ResponseEntity<ApiResponse<List<MemberMembershipResponse>>> getMemberMemberships(@PathVariable UUID memberId) {
    List<MemberMembership> memberships = membershipService.getMembershipHistory(memberId);
    List<MemberMembershipResponse> responses = memberships.stream()
      .map(MemberMembershipResponse::from)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/member/{memberId}/active")
  @Operation(summary = "Get active membership", description = "Get current active membership for a member")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> getActiveMembership(@PathVariable UUID memberId) {
    MemberMembership membership = membershipService.getActiveMembership(memberId);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership)));
  }

  @GetMapping("/gym/{gymId}")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'SUPER_ADMIN')")
  @Operation(summary = "Get gym's memberships", description = "Get all memberships for a gym")
  public ResponseEntity<ApiResponse<List<MemberMembershipResponse>>> getGymMemberships(
    @PathVariable UUID gymId,
    @RequestParam(required = false) MembershipStatus status) {

    List<MemberMembership> memberships;

    if (status != null) {
      memberships = membershipService.getMembershipsByStatus(gymId, status);
    } else {
      memberships = membershipService.getGymMemberships(gymId);
    }

    List<MemberMembershipResponse> responses = memberships.stream()
      .map(MemberMembershipResponse::from)
      .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/expiring")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'SUPER_ADMIN')")
  @Operation(summary = "Get expiring memberships", description = "Get memberships expiring within specified days")
  public ResponseEntity<ApiResponse<List<MemberMembershipResponse>>> getExpiringMemberships(
    @PathVariable UUID gymId,
    @RequestParam(defaultValue = "30") int daysAhead) {

    List<MemberMembership> memberships = membershipService.getExpiringMemberships(gymId, daysAhead);
    List<MemberMembershipResponse> responses = memberships.stream()
      .map(MemberMembershipResponse::from)
      .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/count")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'SUPER_ADMIN')")
  @Operation(summary = "Count active memberships", description = "Get count of active memberships for a gym")
  public ResponseEntity<ApiResponse<Long>> getActiveMembershipCount(@PathVariable UUID gymId) {
    long count = membershipService.getActiveMembershipCount(gymId);
    return ResponseEntity.ok(ApiResponse.success(count));
  }

  @PutMapping("/{membershipId}/freeze")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'MEMBER', 'SUPER_ADMIN')")
  @Operation(summary = "Freeze membership", description = "Freeze/hold a membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> freezeMembership(
    @PathVariable UUID membershipId,
    @Valid @RequestBody FreezeMembershipRequest request) {

    MemberMembership membership = membershipService.freezeMembership(
      membershipId,
      request.freezeUntil(),
      request.reason()
    );

    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership), "Membership frozen"));
  }

  @PutMapping("/{membershipId}/unfreeze")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'MEMBER', 'SUPER_ADMIN')")
  @Operation(summary = "Unfreeze membership", description = "Unfreeze a frozen membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> unfreezeMembership(@PathVariable UUID membershipId) {
    MemberMembership membership = membershipService.unfreezeMembership(membershipId);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership), "Membership unfrozen"));
  }

  @PutMapping("/{membershipId}/renew")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'SUPER_ADMIN')")
  @Operation(summary = "Renew membership", description = "Manually renew a membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> renewMembership(@PathVariable UUID membershipId) {
    MemberMembership membership = membershipService.renewMembership(membershipId);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership), "Membership renewed"));
  }

  @PutMapping("/{membershipId}/cancel")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'MEMBER', 'SUPER_ADMIN')")
  @Operation(summary = "Cancel membership", description = "Cancel a membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> cancelMembership(
    @PathVariable UUID membershipId,
    @RequestParam(defaultValue = "false") boolean immediate) {

    MemberMembership membership = membershipService.cancelMembership(membershipId, immediate);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership), "Membership cancelled"));
  }

  @PutMapping("/{membershipId}/use-credit")
  @PreAuthorize("hasAnyRole('GYM_OWNER', 'STAFF', 'MEMBER', 'SUPER_ADMIN')")
  @Operation(summary = "Use class credit", description = "Use a class credit from membership")
  public ResponseEntity<ApiResponse<MemberMembershipResponse>> useClassCredit(@PathVariable UUID membershipId) {
    MemberMembership membership = membershipService.useClassCredit(membershipId);
    return ResponseEntity.ok(ApiResponse.success(MemberMembershipResponse.from(membership), "Class credit used"));
  }
}
