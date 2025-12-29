package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.*;
import com.gymmate.payment.application.RefundRequestService;
import com.gymmate.payment.application.StripePaymentService;
import com.gymmate.payment.domain.RefundAuditLog;
import com.gymmate.payment.domain.RefundType;
import com.gymmate.shared.dto.ApiResponse;
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
 * REST controller for gym payment management.
 * Handles payment methods, invoices, and refund workflows for subscriptions.
 */
@RestController
@RequestMapping("/api/subscriptions/payments")
@RequiredArgsConstructor
@Tag(name = "Subscription Payments", description = "Payment management for gym subscriptions")
public class PaymentController {

    private final StripePaymentService stripePaymentService;
    private final RefundRequestService refundRequestService;

    @PostMapping("/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Attach payment method", description = "Attach a new payment method to the gym's subscription")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> attachPaymentMethod(
            @Valid @RequestBody AttachPaymentMethodRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        PaymentMethodResponse response = stripePaymentService.attachPaymentMethod(
                gymId,
                request.getStripePaymentMethodId(),
                request.getSetAsDefault() != null ? request.getSetAsDefault() : true
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Payment method attached successfully"));
    }

    @GetMapping("/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get payment methods", description = "Get all payment methods for the gym")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<PaymentMethodResponse> methods = stripePaymentService.getPaymentMethods(gymId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @DeleteMapping("/methods/{id}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove payment method", description = "Remove a payment method from the gym")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(@PathVariable UUID id) {
        UUID gymId = TenantContext.getCurrentTenantId();
        stripePaymentService.removePaymentMethod(gymId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed successfully"));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get invoices", description = "Get all invoices for the gym's subscription")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<InvoiceResponse> invoices = stripePaymentService.getInvoices(gymId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/refunds")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Process refund", description = "Process a full or partial refund for a payment")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        RefundResponse response = stripePaymentService.processRefund(gymId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund history", description = "Get all refunds for the gym")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundHistory() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundResponse> refunds = stripePaymentService.getRefundHistory(gymId);
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }

    @GetMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund details", description = "Get details of a specific refund")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(@PathVariable UUID refundId) {
        UUID gymId = TenantContext.getCurrentTenantId();
        RefundResponse response = stripePaymentService.getRefund(gymId, refundId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== Refund Request Workflow Endpoints =====

    @PostMapping("/refund-requests")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create refund request",
               description = "Create a new refund request for platform subscription (Gym owners request refunds from GymMate)")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> createPlatformRefundRequest(
            @Valid @RequestBody CreateRefundRequestDTO request,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        UUID gymId = TenantContext.getCurrentTenantId();

        // For platform subscription refunds, the gym owner is both requester and recipient
        RefundRequestResponse response = refundRequestService.createRefundRequest(
                gymId,
                currentUser.getUserId(),
                currentUser.getRole(),
                currentUser.getUserId(), // Refund goes back to gym owner
                "GYM_OWNER",
                request
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request submitted successfully"));
    }

    @GetMapping("/refund-requests")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund requests", description = "Get all refund requests for the gym")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getAllRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/refund-requests/pending")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get pending refund requests", description = "Get all pending refund requests awaiting action")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getPendingRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getPendingRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/refund-requests/{requestId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund request", description = "Get details of a specific refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getRefundRequest(@PathVariable UUID requestId) {
        RefundRequestResponse response = refundRequestService.getRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/refund-requests/{requestId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Approve refund request",
               description = "Approve a platform subscription refund request (SUPER_ADMIN only)")
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

    @PutMapping("/refund-requests/{requestId}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Reject refund request",
               description = "Reject a platform subscription refund request (SUPER_ADMIN only)")
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

    @PostMapping("/refund-requests/{requestId}/process")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Process approved refund",
               description = "Execute the Stripe refund for an approved request (SUPER_ADMIN only)")
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

    @PutMapping("/refund-requests/{requestId}/cancel")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Cancel refund request", description = "Cancel a pending refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> cancelRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.cancelRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole()
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request cancelled"));
    }

    @GetMapping("/refund-requests/{requestId}/audit")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund request audit trail", description = "Get the complete audit history for a refund request")
    public ResponseEntity<ApiResponse<List<RefundAuditLog>>> getRefundRequestAudit(@PathVariable UUID requestId) {
        List<RefundAuditLog> auditTrail = refundRequestService.getAuditTrail(requestId);
        return ResponseEntity.ok(ApiResponse.success(auditTrail));
    }
}

