package com.gymmate.billing.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.billing.internal.domain.RefundAuditLog;

import java.util.List;
import java.util.UUID;

public interface RefundAuditLogRepository extends DomainRepository<RefundAuditLog, UUID> {

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

