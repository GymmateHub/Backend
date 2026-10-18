package com.gymmate.billing.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RefundAuditLogJpaRepository extends JpaRepository<RefundAuditLogJpaEntity, UUID> {

    /**
     * Find all audit logs for a refund request.
     */
    List<RefundAuditLogJpaEntity> findByRefundRequestIdOrderByCreatedAtAsc(UUID refundRequestId);

    /**
     * Find all audit logs for a payment refund.
     */
    List<RefundAuditLogJpaEntity> findByPaymentRefundIdOrderByCreatedAtAsc(UUID paymentRefundId);

    /**
     * Find audit logs by action type.
     */
    List<RefundAuditLogJpaEntity> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs by performer.
     */
    List<RefundAuditLogJpaEntity> findByPerformedByUserIdOrderByCreatedAtDesc(UUID userId);
}
