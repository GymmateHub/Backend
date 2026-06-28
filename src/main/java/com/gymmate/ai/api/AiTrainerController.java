package com.gymmate.ai.api;

import com.gymmate.ai.api.dto.AiPlanRequest;
import com.gymmate.ai.api.dto.AiPlanResponse;
import com.gymmate.ai.application.AiPlanService;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import com.gymmate.shared.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing AI Personal Trainer endpoints to members and staff.
 *
 * <pre>
 * GET  /api/ai/trainer/my-plan            → member's latest plan (cached)
 * POST /api/ai/trainer/generate-plan      → regenerate plan with optional new goals
 * GET  /api/ai/trainer/my-plans           → full plan history
 * GET  /api/ai/trainer/plan/{memberId}    → staff/trainer: view any member's latest plan
 * POST /api/ai/trainer/plan/{memberId}/generate  → staff/trainer: regenerate for a member
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/trainer")
@RequiredArgsConstructor
@Tag(name = "AI Personal Trainer", description = "AI-generated personalised workout and meal plans")
public class AiTrainerController {

    private final AiPlanService aiPlanService;
    private final MemberService memberService;

    // -------------------------------------------------------------------------
    // Member-facing endpoints
    // -------------------------------------------------------------------------

    /**
     * Get the authenticated member's latest AI plan.
     * Served from Redis cache when available (TTL configurable via AI_PLAN_CACHE_TTL_HOURS).
     */
    @GetMapping("/my-plan")
    @PreAuthorize("hasAnyRole('MEMBER', 'OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Get my AI plan",
        description = "Returns the authenticated member's latest personalised AI workout and meal plan. "
            + "Served from cache when available — no new LLM call is made until the cache expires or "
            + "the member explicitly requests a regeneration."
    )
    public ResponseEntity<ApiResponse<AiPlanResponse>> getMyPlan(
        @AuthenticationPrincipal TenantAwareUserDetails userDetails
    ) {
        UUID memberId = resolveMemberId(userDetails.getUserId());
        AiPlanResponse plan = aiPlanService.getOrGeneratePlan(memberId);
        String message = plan.cached() ? "Plan served from cache" : "Plan loaded successfully";
        return ResponseEntity.ok(ApiResponse.success(plan, message));
    }

    /**
     * Generate (or regenerate) the authenticated member's AI plan.
     * Accepts optional new goals — when provided, the member's stored goals are updated first.
     */
    @PostMapping("/generate-plan")
    @PreAuthorize("hasAnyRole('MEMBER', 'OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Generate / refresh my AI plan",
        description = "Generates a fresh AI workout and meal plan for the authenticated member. "
            + "Optionally accepts new fitness goals and experience level which are persisted on the "
            + "member profile. The Redis cache is invalidated so the next GET returns the new plan."
    )
    public ResponseEntity<ApiResponse<AiPlanResponse>> generateMyPlan(
        @AuthenticationPrincipal TenantAwareUserDetails userDetails,
        @Valid @RequestBody(required = false) AiPlanRequest request
    ) {
        UUID memberId = resolveMemberId(userDetails.getUserId());
        AiPlanResponse plan = aiPlanService.regeneratePlan(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(plan, "New AI plan generated successfully"));
    }

    /**
     * Get the full plan history for the authenticated member (newest first).
     */
    @GetMapping("/my-plans")
    @PreAuthorize("hasAnyRole('MEMBER', 'OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Get my AI plan history",
        description = "Returns all previously generated AI plans for the authenticated member, newest first."
    )
    public ResponseEntity<ApiResponse<List<AiPlanResponse>>> getMyPlanHistory(
        @AuthenticationPrincipal TenantAwareUserDetails userDetails
    ) {
        UUID memberId = resolveMemberId(userDetails.getUserId());
        List<AiPlanResponse> history = aiPlanService.getPlanHistory(memberId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    // -------------------------------------------------------------------------
    // Staff / Trainer endpoints
    // -------------------------------------------------------------------------

    /**
     * Get the latest plan for any member — accessible by TRAINER, STAFF, OWNER, ADMIN.
     */
    @GetMapping("/plan/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Get AI plan for a specific member",
        description = "Allows trainers and staff to view the latest AI plan for any member "
            + "within their organisation."
    )
    public ResponseEntity<ApiResponse<AiPlanResponse>> getMemberPlan(
        @Parameter(description = "Member UUID") @PathVariable UUID memberId
    ) {
        AiPlanResponse plan = aiPlanService.getOrGeneratePlan(memberId);
        String message = plan.cached() ? "Plan served from cache" : "Plan loaded successfully";
        return ResponseEntity.ok(ApiResponse.success(plan, message));
    }

    /**
     * Regenerate the AI plan for any member — accessible by TRAINER, STAFF, OWNER, ADMIN.
     */
    @PostMapping("/plan/{memberId}/generate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Generate AI plan for a specific member",
        description = "Allows trainers and staff to trigger a fresh AI plan generation for any member."
    )
    public ResponseEntity<ApiResponse<AiPlanResponse>> generateMemberPlan(
        @Parameter(description = "Member UUID") @PathVariable UUID memberId,
        @Valid @RequestBody(required = false) AiPlanRequest request
    ) {
        AiPlanResponse plan = aiPlanService.regeneratePlan(memberId, request);
        return ResponseEntity.ok(ApiResponse.success(plan, "New AI plan generated successfully for member"));
    }

    /**
     * Get plan history for any member — accessible by TRAINER, STAFF, OWNER, ADMIN.
     */
    @GetMapping("/plan/{memberId}/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(
        summary = "Get AI plan history for a specific member",
        description = "Returns all previously generated AI plans for a member, newest first."
    )
    public ResponseEntity<ApiResponse<List<AiPlanResponse>>> getMemberPlanHistory(
        @Parameter(description = "Member UUID") @PathVariable UUID memberId
    ) {
        List<AiPlanResponse> history = aiPlanService.getPlanHistory(memberId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Resolves the Member ID from the authenticated user's UUID.
     * Throws {@link ResourceNotFoundException} when the user has no member profile.
     */
    private UUID resolveMemberId(UUID userId) {
        Member member = memberService.findByUserId(userId);
        return member.getId();
    }
}

