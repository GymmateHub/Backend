package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.RefundAuditLog;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface RefundAuditLogRepository {

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
    
    RefundAuditLog save(RefundAuditLog entity);
    
    List<RefundAuditLog> saveAll(Iterable<RefundAuditLog> entities);
    
    Optional<RefundAuditLog> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<RefundAuditLog> findAll();
    
    List<RefundAuditLog> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(RefundAuditLog entity);
    
    void deleteAll(Iterable<RefundAuditLog> entities);
    
    RefundAuditLog saveAndFlush(RefundAuditLog entity);
    
    void flush();
    
    Page<RefundAuditLog> findAll(Pageable pageable);
}

