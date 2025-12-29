package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.RefundAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundAuditLogRepository extends JpaRepository<RefundAuditLog, UUID> {

    /**
     * Find all audit logs for a refund request.
     */
    List<RefundAuditLog> findByRefundRequestIdOrderByCreatedAtAsc(UUID refundRequestId);

    /**
     * Find all audit logs for a payment refund.
     */
    List<RefundAuditLog> findByPaymentRefundIdOrderByCreatedAtAsc(UUID paymentRefundId);

    /**
     * Find audit logs by action type.
     */
    List<RefundAuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs by performer.
     */
    List<RefundAuditLog> findByPerformedByUserIdOrderByCreatedAtDesc(UUID userId);
}

