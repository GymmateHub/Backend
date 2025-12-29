package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.CreateRefundRequestDTO;
import com.gymmate.payment.api.dto.RefundRequestResponse;
import com.gymmate.payment.application.RefundRequestService;
import com.gymmate.payment.domain.RefundType;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for member refund requests.
 * Allows gym members to request refunds for their payments (memberships, classes, etc.).
 */
@RestController
@RequestMapping("/api/members/refunds")
@RequiredArgsConstructor
@Tag(name = "Member Refunds", description = "Refund requests for gym members")
public class MemberRefundController {

    private final RefundRequestService refundRequestService;

    @PostMapping("/request")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Request refund",
               description = "Submit a refund request for a payment (membership, class booking, etc.)")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> requestRefund(
            @Valid @RequestBody CreateRefundRequestDTO request,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        UUID gymId = TenantContext.getCurrentTenantId();

        // Ensure the request is for member payment type
        if (request.getRefundType() != RefundType.MEMBER_PAYMENT) {
            request.setRefundType(RefundType.MEMBER_PAYMENT);
        }

        // Member requests refund, and the refund goes back to the member
        RefundRequestResponse response = refundRequestService.createRefundRequest(
                gymId,
                currentUser.getUserId(),
                "MEMBER",
                currentUser.getUserId(), // Refund goes back to the member
                "MEMBER",
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request submitted successfully"));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my refund requests", description = "Get all refund requests submitted by the current member")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getMyRefundRequests(
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        List<RefundRequestResponse> requests = refundRequestService.getMyRequests(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/my-requests/{requestId}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get refund request", description = "Get details of a specific refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getMyRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.getRequest(requestId);

        // Verify the request belongs to this member
        if (!response.getRequestedByUserId().equals(currentUser.getUserId())) {
            throw new DomainException("ACCESS_DENIED", "You can only view your own refund requests");
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/my-requests/{requestId}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Cancel refund request", description = "Cancel a pending refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> cancelMyRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.cancelRequest(
                requestId,
                currentUser.getUserId(),
                "MEMBER"
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request cancelled"));
    }
}

