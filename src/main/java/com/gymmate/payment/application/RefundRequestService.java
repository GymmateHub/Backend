package com.gymmate.payment.application;

import com.gymmate.payment.api.dto.CreateRefundRequestDTO;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundRequestResponse;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.*;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
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
    private final UserService userService;

    // Default SLA: 3 business days
    private static final int DEFAULT_SLA_DAYS = 3;

    /**
     * Create a new refund request (by member or gym owner).
     */
    @Transactional
    public RefundRequestResponse createRefundRequest(
            UUID gymId,
            UUID requestedByUserId,
            String requestedByType,
            UUID refundToUserId,
            String refundToType,
            CreateRefundRequestDTO dto) {

        // Validate requested amount doesn't exceed original
        if (dto.getRequestedRefundAmount().compareTo(dto.getOriginalPaymentAmount()) > 0) {
            throw new DomainException("INVALID_REFUND_AMOUNT",
                "Requested refund amount cannot exceed original payment amount");
        }

        // Check for existing pending request for same payment
        refundRequestRepository.findByStripePaymentIntentIdAndStatus(
                dto.getStripePaymentIntentId(), RefundRequestStatus.PENDING)
            .ifPresent(existing -> {
                throw new DomainException("DUPLICATE_REFUND_REQUEST",
                    "A pending refund request already exists for this payment");
            });

        // Create refund request
        RefundRequestEntity request = RefundRequestEntity.builder()
                .gymId(gymId)
                .refundType(dto.getRefundType())
                .stripePaymentIntentId(dto.getStripePaymentIntentId())
                .stripeChargeId(dto.getStripeChargeId())
                .originalPaymentAmount(dto.getOriginalPaymentAmount())
                .requestedRefundAmount(dto.getRequestedRefundAmount())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .membershipId(dto.getMembershipId())
                .classBookingId(dto.getClassBookingId())
                .requestedByUserId(requestedByUserId)
                .requestedByType(requestedByType)
                .refundToUserId(refundToUserId)
                .refundToType(refundToType)
                .reasonCategory(dto.getReasonCategory())
                .reasonDescription(dto.getReasonDescription())
                .supportingEvidence(dto.getSupportingEvidence())
                .status(RefundRequestStatus.PENDING)
                .dueBy(LocalDateTime.now().plusDays(DEFAULT_SLA_DAYS))
                .build();

        RefundRequestEntity saved = refundRequestRepository.save(request);

        // Create audit log
        RefundAuditLog auditLog = RefundAuditLog.created(saved, requestedByUserId, requestedByType);
        auditLogRepository.save(auditLog);

        log.info("Created refund request {} for payment {} by user {} ({})",
            saved.getId(), dto.getStripePaymentIntentId(), requestedByUserId, requestedByType);

        return toResponse(saved);
    }

    /**
     * Get all pending refund requests for a gym (for owner dashboard).
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getPendingRequests(UUID gymId) {
        return refundRequestRepository.findPendingByGymId(gymId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all refund requests for a gym.
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getAllRequests(UUID gymId) {
        return refundRequestRepository.findByGymIdOrderByCreatedAtDesc(gymId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get refund requests made by a specific user (member view).
     */
    @Transactional(readOnly = true)
    public List<RefundRequestResponse> getMyRequests(UUID userId) {
        return refundRequestRepository.findByRequestedByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific refund request.
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

        String oldStatus = request.getStatus().name();
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
        RefundRequest stripeRequest = RefundRequest.builder()
                .paymentIntentId(request.getStripePaymentIntentId())
                .amount(request.getRequestedRefundAmount())
                .reason(request.getReasonDescription())
                .build();

        // Process the actual refund via Stripe
        RefundResponse refundResponse = stripePaymentService.processRefund(
            request.getGymId(), stripeRequest);

        // Update the PaymentRefund with additional tracking info
        PaymentRefund paymentRefund = paymentRefundRepository
            .findByStripeRefundId(refundResponse.getRefundId())
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
            requestId, refundResponse.getRefundId());

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

    private RefundRequestEntity findRequestById(UUID requestId) {
        return refundRequestRepository.findById(requestId)
            .orElseThrow(() -> new DomainException("REFUND_REQUEST_NOT_FOUND",
                "Refund request not found: " + requestId));
    }

    private RefundRequestResponse toResponse(RefundRequestEntity request) {
        RefundRequestResponse.RefundRequestResponseBuilder builder = RefundRequestResponse.builder()
                .id(request.getId())
                .gymId(request.getGymId())
                .refundType(request.getRefundType())
                .stripePaymentIntentId(request.getStripePaymentIntentId())
                .originalPaymentAmount(request.getOriginalPaymentAmount())
                .requestedRefundAmount(request.getRequestedRefundAmount())
                .currency(request.getCurrency())
                .membershipId(request.getMembershipId())
                .classBookingId(request.getClassBookingId())
                .requestedByUserId(request.getRequestedByUserId())
                .requestedByType(request.getRequestedByType())
                .refundToUserId(request.getRefundToUserId())
                .refundToType(request.getRefundToType())
                .reasonCategory(request.getReasonCategory())
                .reasonDescription(request.getReasonDescription())
                .status(request.getStatus())
                .rejectionReason(request.getRejectionReason())
                .processorNotes(request.getProcessorNotes())
                .processedByUserId(request.getProcessedByUserId())
                .processedByType(request.getProcessedByType())
                .processedAt(request.getProcessedAt())
                .dueBy(request.getDueBy())
                .escalated(request.getEscalated())
                .escalatedTo(request.getEscalatedTo())
                .paymentRefundId(request.getPaymentRefundId())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt());

        // Populate names if user service is available
        try {
            User requester = userService.findById(request.getRequestedByUserId());
            builder.requestedByName(requester.getFirstName() + " " + requester.getLastName());
        } catch (Exception e) {
            // User not found or service unavailable
        }

        try {
            User recipient = userService.findById(request.getRefundToUserId());
            builder.refundToName(recipient.getFirstName() + " " + recipient.getLastName());
        } catch (Exception e) {
            // User not found or service unavailable
        }

        if (request.getProcessedByUserId() != null) {
            try {
                User processor = userService.findById(request.getProcessedByUserId());
                builder.processedByName(processor.getFirstName() + " " + processor.getLastName());
            } catch (Exception e) {
                // User not found or service unavailable
            }
        }

        return builder.build();
    }
}

