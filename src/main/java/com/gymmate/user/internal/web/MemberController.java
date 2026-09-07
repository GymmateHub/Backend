package com.gymmate.user.internal.web;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.api.dto.MemberCreateRequest;
import com.gymmate.user.api.dto.MemberResponse;
import com.gymmate.user.api.dto.MemberUpdateRequest;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.application.port.MemberLimitGuard;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * REST controller for member management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member Management APIs")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final MemberLimitGuard limitService;

    private MemberResponse mapWithUser(Member member) {
        User user = null;
        if (member.getUserId() != null) {
            user = userRepository.findById(member.getUserId()).orElse(null);
        }
        return MemberResponse.fromEntity(member, user);
    }

    private List<MemberResponse> mapWithUsers(List<Member> members) {
        if (members == null || members.isEmpty()) return Collections.emptyList();
        Set<UUID> userIds = members.stream()
                .map(Member::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return members.stream()
                .map(m -> MemberResponse.fromEntity(m, userMap.get(m.getUserId())))
                .toList();
    }

    /**
     * Create a new member profile.
     * Checks organisation member limit before creating.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create member", description = "Create a new member profile")
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(@Valid @RequestBody MemberCreateRequest request) {
        // Check organisation member limit
        UUID organisationId = TenantContext.getCurrentTenantId();
        if (organisationId != null) {
            limitService.checkCanAddMember(organisationId);
        }

        Member member;
        if (request.email() != null && !request.email().isBlank()) {
            member = memberService.createMemberWithDetails(
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    request.phone(),
                    request.gymId(),
                    request.membershipNumber()
            );
        } else if (request.userId() != null) {
            member = memberService.createMember(request.userId(), request.gymId(), request.membershipNumber());
        } else {
            throw new IllegalArgumentException("Either email and name or userId must be provided");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapWithUser(member), "Member created successfully"));
    }

    /**
     * Get member by ID.
     * SECURITY: Validates the fetched member belongs to the caller's organisation.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable UUID id) {
        Member member = memberService.findById(id);
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && member.getOrganisationId() != null
                && !tenantId.equals(member.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this member"));
        }
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member)));
    }

    /**
     * Get member by user ID.
     * SECURITY: Validates the fetched member belongs to the caller's organisation.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberByUserId(@PathVariable UUID userId) {
        Member member = memberService.findByUserId(userId);
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && member.getOrganisationId() != null
                && !tenantId.equals(member.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this member"));
        }
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member)));
    }

    /**
     * Get member by membership number.
     * SECURITY: Validates the fetched member belongs to the caller's organisation.
     */
    @GetMapping("/membership-number/{number}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberByMembershipNumber(@PathVariable String number) {
        Member member = memberService.findByMembershipNumber(number);
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && member.getOrganisationId() != null
                && !tenantId.equals(member.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this member"));
        }
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member)));
    }

    /**
     * Get all members in current organisation.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get all members", description = "Get all members in the current organisation")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getAllMembers() {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationId(organisationId);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Get members by gym ID within the organisation.
     */
    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get members by gym", description = "Get all members for a specific gym")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembersByGym(@PathVariable UUID gymId) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationIdAndGymId(organisationId, gymId);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Get all members across organisation (organisation-scoped).
     */
    @GetMapping("/organisation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get organisation members", description = "Get all members across all gyms in the organisation")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getOrganisationMembers() {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationId(organisationId);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Get active members.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get active members", description = "Get all active members in current organisation")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getActiveMembers() {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationIdAndStatus(organisationId, MemberStatus.ACTIVE);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Get members by status.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get members by status", description = "Get members by status in current organisation")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembersByStatus(@PathVariable MemberStatus status) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationIdAndStatus(organisationId, status);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Get members without waiver.
     */
    @GetMapping("/without-waiver")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get members without waiver", description = "Get members who haven't signed waiver in current organisation")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembersWithoutWaiver() {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        List<Member> members = memberRepository.findByOrganisationIdAndWaiverSignedFalse(organisationId);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }

    /**
     * Update member emergency contact.
     */
    @PutMapping("/{id}/emergency-contact")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateEmergencyContact(
            @PathVariable UUID id,
            @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateEmergencyContact(
                id,
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                request.emergencyContactRelationship()
        );
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Emergency contact updated successfully"));
    }

    /**
     * Sign waiver.
     */
    @PatchMapping("/{id}/sign-waiver")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> signWaiver(@PathVariable UUID id) {
        Member member = memberService.signWaiver(id);
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Waiver signed successfully"));
    }

    /**
     * Update health information.
     */
    @PutMapping("/{id}/health-info")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateHealthInfo(
            @PathVariable UUID id,
            @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateHealthInfo(
                id,
                request.medicalConditions(),
                request.allergies(),
                request.medications()
        );
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Health information updated successfully"));
    }

    /**
     * Update fitness goals.
     */
    @PutMapping("/{id}/fitness-goals")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    public ResponseEntity<ApiResponse<MemberResponse>> updateFitnessGoals(
            @PathVariable UUID id,
            @Valid @RequestBody MemberUpdateRequest request) {
        Member member = memberService.updateFitnessGoals(
                id,
                request.fitnessGoals(),
                request.experienceLevel()
        );
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Fitness goals updated successfully"));
    }

    /**
     * Activate member.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<MemberResponse>> activate(@PathVariable UUID id) {
        Member member = memberService.activate(id);
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Member activated successfully"));
    }

    /**
     * Suspend member.
     */
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<MemberResponse>> suspend(@PathVariable UUID id) {
        Member member = memberService.suspend(id);
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Member suspended successfully"));
    }

    /**
     * Cancel member.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<MemberResponse>> cancel(@PathVariable UUID id) {
        Member member = memberService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(mapWithUser(member), "Member cancelled successfully"));
    }

    /**
     * Get member count by status within current organisation.
     */
    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Long>> countByStatus(@PathVariable MemberStatus status) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        long count = memberService.countByStatus(organisationId, status);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * Get new members after a specific date within current organisation.
     */
    @GetMapping("/new-since/{date}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getNewMembers(@PathVariable String date) {
        UUID organisationId = TenantContext.requireCurrentTenantId();
        LocalDate afterDate = LocalDate.parse(date);
        List<Member> members = memberService.findNewMembers(organisationId, afterDate);
        return ResponseEntity.ok(ApiResponse.success(mapWithUsers(members)));
    }
}
