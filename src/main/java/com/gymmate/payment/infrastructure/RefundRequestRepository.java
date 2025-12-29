package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.RefundRequestEntity;
import com.gymmate.payment.domain.RefundRequestStatus;
import com.gymmate.payment.domain.RefundType;
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
     * Find all refund requests for a gym, ordered by creation date.
     */
    List<RefundRequestEntity> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    /**
     * Find all refund requests by status for a gym.
     */
    List<RefundRequestEntity> findByGymIdAndStatusOrderByCreatedAtDesc(UUID gymId, RefundRequestStatus status);

    /**
     * Find all pending refund requests for a gym (for owner dashboard).
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.gymId = :gymId " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.createdAt ASC")
    List<RefundRequestEntity> findPendingByGymId(@Param("gymId") UUID gymId);

    /**
     * Find all refund requests made by a specific user.
     */
    List<RefundRequestEntity> findByRequestedByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all refund requests for a specific recipient.
     */
    List<RefundRequestEntity> findByRefundToUserIdOrderByCreatedAtDesc(UUID userId);

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
     * Count pending requests for a gym.
     */
    long countByGymIdAndStatus(UUID gymId, RefundRequestStatus status);

    /**
     * Find by refund type for analytics.
     */
    List<RefundRequestEntity> findByGymIdAndRefundTypeOrderByCreatedAtDesc(UUID gymId, RefundType refundType);

    /**
     * Find platform subscription refund requests (for SUPER_ADMIN).
     */
    @Query("SELECT r FROM RefundRequestEntity r WHERE r.refundType = 'PLATFORM_SUBSCRIPTION' " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW') " +
           "ORDER BY r.createdAt ASC")
    List<RefundRequestEntity> findPendingPlatformRefunds();

    /**
     * Calculate total refund amount requested within date range.
     */
    @Query("SELECT COALESCE(SUM(r.requestedRefundAmount), 0) FROM RefundRequestEntity r " +
           "WHERE r.gymId = :gymId " +
           "AND r.status = 'PROCESSED' " +
           "AND r.createdAt BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumProcessedRefundsByGymIdAndDateRange(
            @Param("gymId") UUID gymId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

