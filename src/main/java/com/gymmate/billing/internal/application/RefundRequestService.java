package com.gymmate.billing.internal.application;

import com.gymmate.billing.internal.domain.PaymentRefund;
import com.gymmate.billing.internal.domain.RefundAuditLog;
import com.gymmate.billing.internal.domain.RefundRequestEntity;
import com.gymmate.billing.internal.application.port.PaymentRefundRepository;
import com.gymmate.billing.internal.application.port.RefundAuditLogRepository;
import com.gymmate.billing.internal.application.port.RefundRequestRepository;
import com.gymmate.billing.internal.application.dto.CreateRefundRequestDTO;
import com.gymmate.billing.internal.application.dto.RefundRequest;
import com.gymmate.billing.internal.application.dto.RefundRequestResponse;
import com.gymmate.billing.internal.application.dto.RefundResponse;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing refund request workflow.
 * Handles creation, approval, rejection, and processing of refund requests.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefundRequestService {

    private final RefundRequestRepository refundRequestRepository;
    private final RefundAuditLogRepository auditLogRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final StripePaymentService stripePaymentService;
    private final IdentityApi identityApi;
    private final com.gymmate.shared.multitenancy.TenantValidationService tenantValidationService;

    // Default SLA: 3 business days
    private static final int DEFAULT_SLA_DAYS = 3;

  /**
   * Create a new refund request (by member or gym owner).
   * SECURITY: Validates tenant isolation.
   */
  @Transactional
  public RefundRequestResponse createRefundRequest(
    UUID gymId,
    UUID requestedByUserId,
    String requestedByType,
    UUID refundToUserId,
    String refundToType,
    CreateRefundRequestDTO dto) {

    // SECURITY: Get current tenant ID for validation
    UUID organisationId = tenantValidationService.requireCurrentTenantId();

    // Validate requested amount doesn't exceed original
    if (dto.requestedRefundAmount().compareTo(dto.originalPaymentAmount()) > 0) {
      throw new DomainException("INVALID_REFUND_AMOUNT",
        "Requested refund amount cannot exceed original payment amount");
    }

    // Check for existing pending request for same payment
    refundRequestRepository.findByStripePaymentIntentIdAndStatus(
        dto.stripePaymentIntentId(), RefundRequestStatus.PENDING)
      .ifPresent(existing -> {
        // SECURITY: Validate the existing request belongs to current tenant
        tenantValidationService.validateTenantAccess(
          existing.getOrganisationId(), "RefundRequest", existing.getId());
        throw new DomainException("DUPLICATE_REFUND_REQUEST",
          "A pending refund request already exists for this payment");
      });

    // Create refund request
    RefundRequestEntity request = RefundRequestEntity.builder()
      .refundType(dto.refundType())
      .stripePaymentIntentId(dto.stripePaymentIntentId())
      .stripeChargeId(dto.stripeChargeId())
      .originalPaymentAmount(dto.originalPaymentAmount())
      .requestedRefundAmount(dto.requestedRefundAmount())
      .currency(dto.currency() != null ? dto.currency() : "USD")
      .membershipId(dto.membershipId())
      .classBookingId(dto.classBookingId())
      .requestedByUserId(requestedByUserId)
      .requestedByType(requestedByType)
      .refundToUserId(refundToUserId)
      .refundToType(refundToType)
      .reasonCategory(dto.reasonCategory())
      .reasonDescription(dto.reasonDescription())
      .supportingEvidence(dto.supportingEvidence())
      .status(RefundRequestStatus.PENDING)
      .dueBy(LocalDateTime.now().plusDays(DEFAULT_SLA_DAYS))
      .build();
    request.setOrganisationId(organisationId); // SECURITY: Set organisation ID
    request.setGymId(gymId); // SECURITY: Set gym ID

    RefundRequestEntity saved = refundRequestRepository.save(request);

    // Create audit log
    RefundAuditLog auditLog = RefundAuditLog.created(saved, requestedByUserId, requestedByType);
    auditLogRepository.save(auditLog);

    log.info("Created refund request {} for payment {} by user {} ({})",
      saved.getId(), dto.stripePaymentIntentId(), requestedByUserId, requestedByType);

    return toResponse(saved);
  }

    /**
     * Get all pending refund requests for a gym (for owner dashboard).
     * SECURITY: Filtered by organisation to prevent cross-tenant access.
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getPendingRequests(UUID gymId) {
        UUID organisationId = tenantValidationService.requireCurrentTenantId();
        return refundRequestRepository.findPendingByGymIdAndOrganisationId(gymId, organisationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all refund requests for a gym.
     * SECURITY: Filtered by organisation to prevent cross-tenant access.
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getAllRequests(UUID gymId) {
        UUID organisationId = tenantValidationService.requireCurrentTenantId();
        return refundRequestRepository.findByGymIdAndOrganisationIdOrderByCreatedAtDesc(gymId, organisationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get refund requests made by a specific user (member view).
     * SECURITY: Filtered by organisation to prevent cross-tenant access.
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getMyRequests(UUID userId) {
        UUID organisationId = tenantValidationService.requireCurrentTenantId();
        return refundRequestRepository.findByRequestedByUserIdAndOrganisationIdOrderByCreatedAtDesc(
                userId, organisationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific refund request.
     * SECURITY: Validates organisation ownership to prevent unauthorized access.
     */
    @Transactional(readOnly = true)
    public RefundRequestResponse getRequest(UUID requestId) {
        RefundRequestEntity request = findRequestById(requestId);
        return toResponse(request);
    }

    /**
     * Approve a refund request (by gym owner or admin).
     */
    @Transactional
    public RefundRequestResponse approveRequest(
            UUID requestId,
            UUID approverId,
            String approverType,
            String notes) {

        RefundRequestEntity request = findRequestById(requestId);

        if (!request.canBeApproved()) {
            throw new DomainException("CANNOT_APPROVE_REQUEST",
                    "Refund request cannot be approved in current status: " + request.getStatus());
        }

        request.approve(approverId, approverType, notes);
        RefundRequestEntity saved = refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.approved(saved, approverId, approverType, notes);
        auditLogRepository.save(auditLog);

        log.info("Refund request {} approved by {} ({})", requestId, approverId, approverType);

        return toResponse(saved);
    }

    /**
     * Reject a refund request (by gym owner or admin).
     */
    @Transactional
    public RefundRequestResponse rejectRequest(
            UUID requestId,
            UUID rejecterId,
            String rejecterType,
            String rejectionReason,
            String notes) {

        RefundRequestEntity request = findRequestById(requestId);

        if (!request.canBeApproved()) {
            throw new DomainException("CANNOT_REJECT_REQUEST",
                    "Refund request cannot be rejected in current status: " + request.getStatus());
        }

        request.reject(rejecterId, rejecterType, rejectionReason, notes);
        RefundRequestEntity saved = refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.rejected(saved, rejecterId, rejecterType, rejectionReason);
        auditLogRepository.save(auditLog);

        log.info("Refund request {} rejected by {} ({}): {}",
                requestId, rejecterId, rejecterType, rejectionReason);

        return toResponse(saved);
    }

    /**
     * Process an approved refund request (execute the Stripe refund).
     */
    @Transactional
    public RefundResponse processApprovedRequest(
            UUID requestId,
            UUID processorId,
            String processorType) {

        RefundRequestEntity request = findRequestById(requestId);

        if (request.getStatus() != RefundRequestStatus.APPROVED) {
            throw new DomainException("REQUEST_NOT_APPROVED",
                    "Refund request must be approved before processing");
        }

        // Create the refund request DTO for Stripe
        RefundRequest stripeRequest = new RefundRequest(
                request.getStripePaymentIntentId(),
                request.getRequestedRefundAmount(),
                request.getReasonDescription());

        // Process the actual refund via Stripe
        RefundResponse refundResponse = stripePaymentService.processRefund(
                request.getGymId(), stripeRequest);

        // Update the PaymentRefund with additional tracking info
        PaymentRefund paymentRefund = paymentRefundRepository
                .findByStripeRefundId(refundResponse.refundId())
                .orElseThrow(() -> new DomainException("REFUND_NOT_FOUND", "Processed refund not found"));

        paymentRefund.setRefundToUserId(request.getRefundToUserId());
        paymentRefund.setRefundToType(request.getRefundToType());
        paymentRefund.setProcessedByUserId(processorId);
        paymentRefund.setProcessedByType(processorType);
        paymentRefund.setRefundRequestId(request.getId());
        paymentRefund.setRefundType(request.getRefundType());
        paymentRefundRepository.save(paymentRefund);

        // Mark request as processed
        request.markProcessed(paymentRefund.getId());
        refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.processed(request, paymentRefund, processorId, processorType);
        auditLogRepository.save(auditLog);

        log.info("Refund request {} processed successfully. Stripe refund: {}",
                requestId, refundResponse.refundId());

        return refundResponse;
    }

    /**
     * Cancel a refund request (by requester).
     */
    @Transactional
    public RefundRequestResponse cancelRequest(UUID requestId, UUID userId, String userType) {
        RefundRequestEntity request = findRequestById(requestId);

        if (!request.canBeCancelled()) {
            throw new DomainException("CANNOT_CANCEL_REQUEST",
                    "Refund request cannot be cancelled in current status: " + request.getStatus());
        }

        // Only the requester or an admin can cancel
        if (!request.getRequestedByUserId().equals(userId) &&
                !userType.equals("SUPER_ADMIN") && !userType.equals("GYM_OWNER")) {
            throw new DomainException("CANCEL_NOT_ALLOWED",
                    "Only the requester or an admin can cancel this request");
        }

        request.cancel();
        RefundRequestEntity saved = refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.cancelled(saved, userId, userType);
        auditLogRepository.save(auditLog);

        log.info("Refund request {} cancelled by {} ({})", requestId, userId, userType);

        return toResponse(saved);
    }

    /**
     * Escalate a refund request for higher-level review.
     */
    @Transactional
    public RefundRequestResponse escalateRequest(
            UUID requestId,
            UUID escalatedBy,
            String escalatedByType,
            String escalateTo) {

        RefundRequestEntity request = findRequestById(requestId);
        request.escalate(escalateTo);
        RefundRequestEntity saved = refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.escalated(saved, escalatedBy, escalatedByType, escalateTo);
        auditLogRepository.save(auditLog);

        log.info("Refund request {} escalated to {} by {} ({})",
                requestId, escalateTo, escalatedBy, escalatedByType);

        return toResponse(saved);
    }

    /**
     * Get audit trail for a refund request.
     */
    @Transactional(readOnly = true)
    public List<RefundAuditLog> getAuditTrail(UUID requestId) {
        return auditLogRepository.findByRefundRequestIdOrderByCreatedAtAsc(requestId);
    }

    /**
     * Get pending platform subscription refunds (for SUPER_ADMIN).
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getPendingPlatformRefunds() {
        return refundRequestRepository.findPendingPlatformRefunds()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ===== Helper Methods =====

    /**
     * Find refund request by ID with tenant validation.
     * SECURITY: This method prevents cross-tenant access by validating organisationId.
     * All methods that access a refund request by ID MUST use this method.
     */
    private RefundRequestEntity findRequestById(UUID requestId) {
        UUID organisationId = tenantValidationService.requireCurrentTenantId();

        RefundRequestEntity request = refundRequestRepository
                .findByIdAndOrganisationId(requestId, organisationId)
                .orElseThrow(() -> new DomainException("REFUND_REQUEST_NOT_FOUND",
                        "Refund request not found or you don't have permission to access it"));

        // SECURITY: Double-check tenant isolation (defense in depth)
        tenantValidationService.validateTenantAccess(
                request.getOrganisationId(), "RefundRequest", request.getId());

        log.debug("Retrieved refund request {} for organisation {}", requestId, organisationId);
        return request;
    }

    private RefundRequestResponse toResponse(RefundRequestEntity request) {
        String processedByName = request.getProcessedByUserId() != null
                ? fullName(request.getProcessedByUserId())
                : null;

        return new RefundRequestResponse(
                request.getId(),
                request.getGymId(),
                request.getRefundType(),
                request.getStripePaymentIntentId(),
                request.getOriginalPaymentAmount(),
                request.getRequestedRefundAmount(),
                request.getCurrency(),
                request.getMembershipId(),
                request.getClassBookingId(),
                request.getRequestedByUserId(),
                request.getRequestedByType(),
                fullName(request.getRequestedByUserId()),
                request.getRefundToUserId(),
                request.getRefundToType(),
                fullName(request.getRefundToUserId()),
                request.getReasonCategory(),
                request.getReasonDescription(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getProcessorNotes(),
                request.getProcessedByUserId(),
                request.getProcessedByType(),
                processedByName,
                request.getProcessedAt(),
                request.getDueBy(),
                request.getEscalated(),
                request.getEscalatedTo(),
                request.getPaymentRefundId(),
                null,
                request.getCreatedAt(),
                request.getUpdatedAt());
    }

    /** The user's display name, or null when the user cannot be looked up. */
    private String fullName(UUID userId) {
        try {
            UserSummary user = identityApi.getUser(userId);
            return user.firstName() + " " + user.lastName();
        } catch (Exception e) {
            // User not found or service unavailable
            return null;
        }
    }
}
