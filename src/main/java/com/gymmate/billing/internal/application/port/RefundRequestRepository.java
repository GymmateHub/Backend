package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.RefundRequestEntity;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefundRequestRepository extends DomainRepository<RefundRequestEntity, UUID> {

    /**
     * Find all refund requests for a gym and organisation, ordered by creation date.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findByGymIdAndOrganisationIdOrderByCreatedAtDesc(
            UUID gymId,
            UUID organisationId);

    /**
     * Find all refund requests by status for a gym and organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findByGymIdAndOrganisationIdAndStatusOrderByCreatedAtDesc(
            UUID gymId,
            UUID organisationId,
            RefundRequestStatus status);

    /**
     * Find all pending refund requests for a gym (for owner dashboard).
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findPendingByGymIdAndOrganisationId(
            UUID gymId,
            UUID organisationId);

    /**
     * Find all refund requests made by a specific user within their organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findByRequestedByUserIdAndOrganisationIdOrderByCreatedAtDesc(
            UUID userId,
            UUID organisationId);

    /**
     * Find all refund requests for a specific recipient within their organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findByRefundToUserIdAndOrganisationIdOrderByCreatedAtDesc(
            UUID userId,
            UUID organisationId);

    /**
     * Find refund requests by payment intent.
     */
    Optional<RefundRequestEntity> findByStripePaymentIntentIdAndStatus(String paymentIntentId, RefundRequestStatus status);

    /**
     * Find escalated requests needing attention.
     */
    List<RefundRequestEntity> findEscalatedRequests();

    /**
     * Find overdue requests (past SLA).
     */
    List<RefundRequestEntity> findOverdueRequests(LocalDateTime now);

    /**
     * Count pending requests for a gym within organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    long countByGymIdAndOrganisationIdAndStatus(
            UUID gymId,
            UUID organisationId,
            RefundRequestStatus status);

    /**
     * Find by refund type for analytics within organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    List<RefundRequestEntity> findByGymIdAndOrganisationIdAndRefundTypeOrderByCreatedAtDesc(
            UUID gymId,
            UUID organisationId,
            RefundType refundType);

    /**
     * Find platform subscription refund requests (for SUPER_ADMIN).
     */
    List<RefundRequestEntity> findPendingPlatformRefunds();

    /**
     * Calculate total refund amount requested within date range for organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    java.math.BigDecimal sumProcessedRefundsByGymIdAndOrganisationIdAndDateRange(
            UUID gymId,
            UUID organisationId,
            LocalDateTime startDate,
            LocalDateTime endDate);

    /**
     * Find a refund request by ID with tenant validation.
     * IMPORTANT: Validates organisationId for tenant isolation.
     */
    Optional<RefundRequestEntity> findByIdAndOrganisationId(
            UUID id,
            UUID organisationId);
}

