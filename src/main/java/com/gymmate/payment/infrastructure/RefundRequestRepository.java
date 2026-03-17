package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.RefundRequestEntity;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequestEntity, UUID> {

    /**
     * Find all refund requests for a gym and organisation, ordered by creation date.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "ORDER BY r.createdAt DESC")
    List<RefundRequestEntity> findByGymIdAndOrganisationIdOrderByCreatedAtDesc(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId);

    /**
     * Find all refund requests by status for a gym and organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "AND r.status = :status " +
           "ORDER BY r.createdAt DESC")
    List<RefundRequestEntity> findByGymIdAndOrganisationIdAndStatusOrderByCreatedAtDesc(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId,
            @Param("status") RefundRequestStatus status);

    /**
     * Find all pending refund requests for a gym (for owner dashboard).
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.createdAt ASC")
    List<RefundRequestEntity> findPendingByGymIdAndOrganisationId(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId);

    /**
     * Find all refund requests made by a specific user within their organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.requestedByUserId = :userId " +
           "AND r.organisationId = :organisationId " +
           "ORDER BY r.createdAt DESC")
    List<RefundRequestEntity> findByRequestedByUserIdAndOrganisationIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("organisationId") UUID organisationId);

    /**
     * Find all refund requests for a specific recipient within their organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.refundToUserId = :userId " +
           "AND r.organisationId = :organisationId " +
           "ORDER BY r.createdAt DESC")
    List<RefundRequestEntity> findByRefundToUserIdAndOrganisationIdOrderByCreatedAtDesc(
            @Param("userId") UUID userId,
            @Param("organisationId") UUID organisationId);

    /**
     * Find refund requests by payment intent.
     */
    Optional<RefundRequestEntity> findByStripePaymentIntentIdAndStatus(String paymentIntentId, RefundRequestStatus status);

    /**
     * Find escalated requests needing attention.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.escalated = true " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.escalatedAt ASC")
    List<RefundRequestEntity> findEscalatedRequests();

    /**
     * Find overdue requests (past SLA).
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.dueBy < :now " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.dueBy ASC")
    List<RefundRequestEntity> findOverdueRequests(@Param("now") LocalDateTime now);

    /**
     * Count pending requests for a gym within organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT COUNT(r) FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "AND r.status = :status")
    long countByGymIdAndOrganisationIdAndStatus(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId,
            @Param("status") RefundRequestStatus status);

    /**
     * Find by refund type for analytics within organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "AND r.refundType = :refundType " +
           "ORDER BY r.createdAt DESC")
    List<RefundRequestEntity> findByGymIdAndOrganisationIdAndRefundTypeOrderByCreatedAtDesc(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId,
            @Param("refundType") RefundType refundType);

    /**
     * Find platform subscription refund requests (for SUPER_ADMIN).
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.refundType = 'PLATFORM_SUBSCRIPTION' " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.createdAt ASC")
    List<RefundRequestEntity> findPendingPlatformRefunds();

    /**
     * Calculate total refund amount requested within date range for organisation.
     * IMPORTANT: Always include organisationId for tenant isolation.
     */
    @Query("SELECT COALESCE(SUM(r.requestedRefundAmount), 0) FROM RefundRequestEntity r " +
           "WHERE r.gymId = :gymId " +
           "AND r.organisationId = :organisationId " +
           "AND r.status = 'PROCESSED' " +
           "AND r.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumProcessedRefundsByGymIdAndOrganisationIdAndDateRange(
            @Param("gymId") UUID gymId,
            @Param("organisationId") UUID organisationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find a refund request by ID with tenant validation.
     * IMPORTANT: Validates organisationId for tenant isolation.
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.id = :id " +
           "AND r.organisationId = :organisationId")
    Optional<RefundRequestEntity> findByIdAndOrganisationId(
            @Param("id") UUID id,
            @Param("organisationId") UUID organisationId);
}

