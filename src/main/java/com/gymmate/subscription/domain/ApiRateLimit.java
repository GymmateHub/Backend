package com.gymmate.subscription.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "api_rate_limits")
public class ApiRateLimit extends BaseAuditEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    // Rate Limit Window
    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "window_type", nullable = false, length = 20)
    @Builder.Default
    private String windowType = "hourly"; // hourly, daily, burst

    // Request Tracking
    @Column(name = "request_count")
    @Builder.Default
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
    @Builder.Default
    private Boolean isBlocked = false;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    // Business Methods
    public void incrementRequest() {
        this.requestCount++;
        checkThreshold();
    }

    private void checkThreshold() {
        if (requestCount >= limitThreshold && !isBlocked) {
            block();
        }
    }

    public void block() {
        this.isBlocked = true;
        this.blockedUntil = calculateBlockDuration();
    }

    private LocalDateTime calculateBlockDuration() {
        // Block until the end of the current window
        return windowEnd;
    }

    public boolean isCurrentlyBlocked() {
        if (!isBlocked) {
            return false;
        }

        if (blockedUntil == null || blockedUntil.isBefore(LocalDateTime.now())) {
            unblock();
            return false;
        }

        return true;
    }

    public void unblock() {
        this.isBlocked = false;
        this.blockedUntil = null;
    }

    public boolean isExpired() {
        return windowEnd.isBefore(LocalDateTime.now());
    }

    public int getRemainingRequests() {
        return Math.max(0, limitThreshold - requestCount);
    }

    public double getUsagePercentage() {
        return (double) requestCount / limitThreshold * 100;
    }
}

