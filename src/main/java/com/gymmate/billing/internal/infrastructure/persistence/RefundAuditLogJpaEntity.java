package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.RefundRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.RefundAuditLog;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link RefundAuditLog} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "RefundAuditLog")
@Table(name = "refund_audit_log")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(RefundAuditLog.class)
public class // ===== Factory Methods =====
RefundAuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "refund_request_id")
    private UUID refundRequestId;

    @Column(name = "payment_refund_id")
    private UUID paymentRefundId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "old_status", length = 30)
    private String oldStatus;

    @Column(name = "new_status", length = 30)
    private String newStatus;

    @Column(name = "performed_by_user_id")
    private UUID performedByUserId;

    @Column(name = "performed_by_type", length = 30)
    private String performedByType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
