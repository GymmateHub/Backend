package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.*;
import com.gymmate.payment.application.PaymentService;
import com.gymmate.payment.application.PaystackWebhookService;
import com.gymmate.payment.application.RefundRequestService;
import com.gymmate.payment.application.StripePaymentService;
import com.gymmate.payment.domain.RefundAuditLog;

import com.gymmate.shared.constants.RefundType;
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
 * REST controller for gym payment management.
 * Handles payment methods, invoices, and refund workflows for subscriptions.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Unified payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundRequestService refundRequestService;
    private final StripePaymentService stripePaymentService;
    private final PaystackWebhookService paystackWebhookService;

    @PostMapping("/subscriptions/payments/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Attach payment method", description = "Attach a new payment method to the gym's subscription")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> attachPaymentMethod(
            @Valid @RequestBody AttachPaymentMethodRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        PaymentMethodResponse response = paymentService.attachPaymentMethod(
                gymId,
                request.getProviderPaymentMethodId(),
                request.getSetAsDefault() != null ? request.getSetAsDefault() : true);

        return ResponseEntity.ok(ApiResponse.success(response, "Payment method attached successfully"));
    }

    @GetMapping("/subscriptions/payments/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get payment methods", description = "Get all payment methods for the gym")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<PaymentMethodResponse> methods = paymentService.getPaymentMethods(gymId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @DeleteMapping("/subscriptions/payments/methods/{id}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove payment method", description = "Remove a payment method from the gym")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(@PathVariable UUID id) {
        UUID gymId = TenantContext.getCurrentTenantId();
        paymentService.removePaymentMethod(gymId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed successfully"));
    }

    @GetMapping("/subscriptions/payments/invoices")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get invoices", description = "Get all invoices for the organisation's subscription")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices() {
        // Organisation ID is used for invoices - the tenant context provides this
        UUID organisationId = TenantContext.getCurrentTenantId();
        List<InvoiceResponse> invoices = paymentService.getInvoicesForOrganisation(organisationId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/subscriptions/payments/refunds")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @Operation(summary = "Process refund", description = "Process a full or partial refund for a payment")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        RefundResponse response = paymentService.processRefund(gymId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    @GetMapping("/subscriptions/payments/refunds")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund history", description = "Get all refunds for the gym")
    public ResponseEntity<ApiResponse<List<RefundResponse>>> getRefundHistory() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundResponse> refunds = paymentService.getRefundHistory(gymId);
        return ResponseEntity.ok(ApiResponse.success(refunds));
    }

    @GetMapping("/subscriptions/payments/refunds/{refundId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund details", description = "Get details of a specific refund")
    public ResponseEntity<ApiResponse<RefundResponse>> getRefund(@PathVariable UUID refundId) {
        UUID gymId = TenantContext.getCurrentTenantId();
        RefundResponse response = paymentService.getRefund(gymId, refundId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== Refund Request Workflow Endpoints =====

    @PostMapping("/subscriptions/payments/refund-requests")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create refund request", description = "Create a new refund request for platform subscription (Gym owners request refunds from GymMate)")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> createPlatformRefundRequest(
            @Valid @RequestBody CreateRefundRequestDTO request,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        UUID gymId = TenantContext.getCurrentTenantId();

        // For platform subscription refunds, the gym owner is both requester and
        // recipient
        RefundRequestResponse response = refundRequestService.createRefundRequest(
                gymId,
                currentUser.getUserId(),
                currentUser.getRole(),
                currentUser.getUserId(), // Refund goes back to gym owner
                "GYM_OWNER",
                request);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request submitted successfully"));
    }

    @GetMapping("/subscriptions/payments/refund-requests")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund requests", description = "Get all refund requests for the gym")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getAllRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/subscriptions/payments/refund-requests/pending")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get pending refund requests", description = "Get all pending refund requests awaiting action")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getPendingRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getPendingRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/subscriptions/payments/refund-requests/{requestId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund request", description = "Get details of a specific refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getRefundRequest(@PathVariable UUID requestId) {
        RefundRequestResponse response = refundRequestService.getRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/subscriptions/payments/refund-requests/{requestId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Approve refund request", description = "Approve a platform subscription refund request (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> approveRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.approveRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                notes);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request approved"));
    }

    @PutMapping("/subscriptions/payments/refund-requests/{requestId}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Reject refund request", description = "Reject a platform subscription refund request (SUPER_ADMIN only)")
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
                notes);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request rejected"));
    }

    @PostMapping("/subscriptions/payments/refund-requests/{requestId}/process")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Process approved refund", description = "Execute the provider refund for an approved request (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundResponse response = refundRequestService.processApprovedRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole());

        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    @PutMapping("/subscriptions/payments/refund-requests/{requestId}/cancel")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Cancel refund request", description = "Cancel a pending refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> cancelRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.cancelRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole());

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request cancelled"));
    }

    @GetMapping("/subscriptions/payments/refund-requests/{requestId}/audit")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get refund request audit trail", description = "Get the complete audit history for a refund request")
    public ResponseEntity<ApiResponse<List<RefundAuditLog>>> getRefundRequestAudit(@PathVariable UUID requestId) {
        List<RefundAuditLog> auditTrail = refundRequestService.getAuditTrail(requestId);
        return ResponseEntity.ok(ApiResponse.success(auditTrail));
    }

    // ===== Gym Owner Refund Management Region =====

    @GetMapping("/gym/refund-requests")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get all gym refund requests", description = "Get all member refund requests for the gym")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getAllGymRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getAllRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/gym/refund-requests/pending")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get pending gym refund requests", description = "Get all pending member refund requests for the gym")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getPendingGymRefundRequests() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<RefundRequestResponse> requests = refundRequestService.getPendingRequests(gymId);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/gym/refund-requests/{requestId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF')")
    @Operation(summary = "Get gym refund request", description = "Get details of a specific member refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getGymRefundRequest(@PathVariable UUID requestId) {
        RefundRequestResponse response = refundRequestService.getRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/gym/refund-requests/{requestId}/approve")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Approve member refund request", description = "Approve a member refund request for gym payments")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> approveGymRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.approveRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                notes);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request approved"));
    }

    @PutMapping("/gym/refund-requests/{requestId}/reject")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Reject member refund request", description = "Reject a member refund request with a reason")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> rejectGymRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam String rejectionReason,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.rejectRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                rejectionReason,
                notes);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request rejected"));
    }

    @PostMapping("/gym/refund-requests/{requestId}/process")
    @PreAuthorize("hasRole('GYM_OWNER')")
    @Operation(summary = "Process gym refund request", description = "Execute provider refund for an approved gym member refund request")
    public ResponseEntity<ApiResponse<RefundResponse>> processGymRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundResponse response = refundRequestService.processApprovedRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole());

        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    @PutMapping("/gym/refund-requests/{requestId}/escalate")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Escalate gym refund request", description = "Escalate a refund request for higher-level review")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> escalateGymRefundRequest(
            @PathVariable UUID requestId,
            @RequestParam String escalateTo,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.escalateRequest(
                requestId,
                currentUser.getUserId(),
                currentUser.getRole(),
                escalateTo);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request escalated"));
    }

    @GetMapping("/gym/refund-requests/{requestId}/audit")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER')")
    @Operation(summary = "Get gym refund request audit", description = "Get complete audit history for a gym refund request")
    public ResponseEntity<ApiResponse<List<RefundAuditLog>>> getGymRefundRequestAudit(@PathVariable UUID requestId) {
        List<RefundAuditLog> auditTrail = refundRequestService.getAuditTrail(requestId);
        return ResponseEntity.ok(ApiResponse.success(auditTrail));
    }

    // ===== Member Refund Management Region =====

    @PostMapping("/members/refunds/request")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Request member refund", description = "Submit a member refund request for membership/class payments")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> requestMemberRefund(
            @Valid @RequestBody CreateRefundRequestDTO request,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        UUID gymId = TenantContext.getCurrentTenantId();
        if (request.getRefundType() != RefundType.MEMBER_PAYMENT) {
            request.setRefundType(RefundType.MEMBER_PAYMENT);
        }

        RefundRequestResponse response = refundRequestService.createRefundRequest(
                gymId,
                currentUser.getUserId(),
                "MEMBER",
                currentUser.getUserId(),
                "MEMBER",
                request);

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request submitted successfully"));
    }

    @GetMapping("/members/refunds/my-requests")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my member refund requests", description = "Get all refund requests submitted by the current member")
    public ResponseEntity<ApiResponse<List<RefundRequestResponse>>> getMyMemberRefundRequests(
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        List<RefundRequestResponse> requests = refundRequestService.getMyRequests(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/members/refunds/my-requests/{requestId}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Get my member refund request", description = "Get details of a specific member-owned refund request")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> getMyMemberRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.getRequest(requestId);
        if (!response.getRequestedByUserId().equals(currentUser.getUserId())) {
            throw new DomainException("ACCESS_DENIED", "You can only view your own refund requests");
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/members/refunds/my-requests/{requestId}/cancel")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "Cancel my member refund request", description = "Cancel a pending refund request submitted by the current member")
    public ResponseEntity<ApiResponse<RefundRequestResponse>> cancelMyMemberRefundRequest(
            @PathVariable UUID requestId,
            @AuthenticationPrincipal TenantAwareUserDetails currentUser) {

        RefundRequestResponse response = refundRequestService.cancelRequest(
                requestId,
                currentUser.getUserId(),
                "MEMBER");

        return ResponseEntity.ok(ApiResponse.success(response, "Refund request cancelled"));
    }

    // ===== Stripe Connect endpoints =====

    @PostMapping("/connect/onboard")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Start onboarding", description = "Start Stripe Connect onboarding to accept member payments")
    public ResponseEntity<ApiResponse<ConnectOnboardingResponse>> startOnboarding() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectOnboardingResponse response = stripePaymentService.startOnboarding(gymId);
        return ResponseEntity.ok(ApiResponse.success(response, "Onboarding started. Redirect to the provided URL."));
    }

    @GetMapping("/connect/status")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get account status", description = "Get the current Stripe Connect account status")
    public ResponseEntity<ApiResponse<ConnectAccountStatusResponse>> getAccountStatus() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectAccountStatusResponse response = stripePaymentService.getAccountStatus(gymId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/connect/refresh")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Refresh onboarding link", description = "Get a new onboarding link if the previous one expired")
    public ResponseEntity<ApiResponse<ConnectOnboardingResponse>> refreshOnboardingLink() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectOnboardingResponse response = stripePaymentService.refreshOnboardingLink(gymId);
        return ResponseEntity.ok(ApiResponse.success(response, "New onboarding link generated."));
    }

    @GetMapping("/connect/dashboard")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get dashboard link", description = "Get a link to the Stripe Express dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboardLink() {
        UUID gymId = TenantContext.getCurrentTenantId();
        String dashboardUrl = stripePaymentService.getDashboardLink(gymId);
        return ResponseEntity.ok(ApiResponse.success(dashboardUrl, "Dashboard link generated."));
    }

    @GetMapping("/connect/can-accept-payments")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Check payment capability", description = "Check if the gym can accept member payments")
    public ResponseEntity<ApiResponse<Boolean>> canAcceptPayments() {
        UUID gymId = TenantContext.getCurrentTenantId();
        boolean canAccept = stripePaymentService.canAcceptPayments(gymId);
        return ResponseEntity.ok(ApiResponse.success(canAccept));
    }

    // ===== Webhook endpoints =====

    @PostMapping("/webhooks/stripe/platform")
    @Operation(summary = "Stripe platform webhook", description = "Handle Stripe platform webhook events")
    public ResponseEntity<String> handlePlatformWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripePaymentService.processPlatformWebhook(payload, signature);
        return ResponseEntity.ok("Processed");
    }

    @PostMapping("/webhooks/stripe/connect")
    @Operation(summary = "Stripe connect webhook", description = "Handle Stripe Connect webhook events")
    public ResponseEntity<String> handleConnectWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        stripePaymentService.processConnectWebhook(payload, signature);
        return ResponseEntity.ok("Processed");
    }

    @PostMapping("/webhooks/paystack")
    @Operation(summary = "Paystack webhook", description = "Handle Paystack webhook events")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader(name = "x-paystack-signature", required = false) String signature) {
        paystackWebhookService.processWebhook(payload, signature);
        return ResponseEntity.ok("Processed");
    }
}
