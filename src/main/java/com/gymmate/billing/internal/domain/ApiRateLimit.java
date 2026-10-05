package com.gymmate.billing.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ApiRateLimit extends BaseAuditEntity {

    private UUID organisationId;

    // Rate Limit Window
    private LocalDateTime windowStart;

    private LocalDateTime windowEnd;

    @Builder.Default
    private 
    String windowType = "hourly"; // hourly, daily, burst

    // Request Tracking
    @Builder.Default
    private 
    Integer requestCount = 0;

    private Integer limitThreshold;

    // Additional Info
    private String endpointPath;

    private String ipAddress;

    private String userAgent;

    // Status
    @Builder.Default
    private 
    Boolean isBlocked = false;

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
