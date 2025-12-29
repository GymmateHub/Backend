package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.RefundRequestResponse;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.application.RefundRequestService;
import com.gymmate.payment.domain.RefundAuditLog;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for gym owners to manage member refund requests.
 * Handles approval, rejection, and processing of member payment refunds.
 */
@RestController
@RequestMapping("/api/gym/refund-requests")
@RequiredArgsConstructor
@Tag(name = "Gym Refund Management", description = "Manage member refund requests (for gym owners)")
public class GymOwnerRefundController {

    private final RefundRequestService refundRequestService;

    @GetMapping
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get all refund requests", description = "Get all refund requests for the gym")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getAllRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getAllRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get pending refund requests",
               description = "Get all pending refund requests awaiting review")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getPendingRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getPendingRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get refund request details", description = "Get details of a specific refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getRefundRequest(@PathVariable UUID requestId) {
        RefundRequestResponse response = refundRequestService.getRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Approve member refund request",
               description = "Approve a member's refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> approveRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.approveRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                notes
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request approved"));
    }

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Reject member refund request",
               description = "Reject a member's refund request with a reason")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> rejectRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam String rejectionReason,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.rejectRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                rejectionReason,
                notes
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request rejected"));
    }

    @PostMapping("/{requestId}/process")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Process approved refund",
               description = "Execute the Stripe refund for an approved member request")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundResponse response = refundRequestService.processApprovedRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    @PutMapping("/{requestId}/escalate")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Escalate refund request",
               description = "Escalate a refund request for higher-level review")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> escalateRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam String escalateTo,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.escalateRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                escalateTo
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request escalated"));
    }

    @GetMapping("/{requestId}/audit")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Get audit trail", description = "Get the complete audit history for a refund request")
    public ResponseEntity<ApiResponse<List<RefundAuditLog>>> getRefundRequestAudit(@PathVariable UUID requestId) {
        List<RefundAuditLog> auditTrail = refundRequestService.getAuditTrail(requestId);
        return ResponseEntity.ok(ApiResponse.success(auditTrail));
    }
}

