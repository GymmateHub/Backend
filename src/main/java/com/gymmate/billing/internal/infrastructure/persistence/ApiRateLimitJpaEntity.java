package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.ApiRateLimit;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ApiRateLimit} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ApiRateLimit")
@Table(name = "api_rate_limits")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ApiRateLimit.class)
public class ApiRateLimitJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "organisation_id", nullable = false)
    private UUID organisationId;

    // Rate Limit Window
    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "window_type", nullable = false, length = 20)
    private String windowType = "hourly"; // hourly, daily, burst

    // Request Tracking
    @Column(name = "request_count")
    private Integer requestCount = 0;

    @Column(name = "limit_threshold", nullable = false)
    private Integer limitThreshold;

    // Additional Info
    @Column(name = "endpoint_path", length = 500)
    private String endpointPath;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // Status
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
}
