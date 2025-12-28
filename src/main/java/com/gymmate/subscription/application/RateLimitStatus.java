package com.gymmate.subscription.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitStatus {
    private UUID gymId;
    private String tierName;

    // Hourly limits
    private Integer hourlyLimit;
    private Integer hourlyUsed;
    private Integer hourlyRemaining;

    // Burst limits (per minute)
    private Integer burstLimit;
    private Integer burstUsed;
    private Integer burstRemaining;

    // Block status
    private Boolean isBlocked;
    private LocalDateTime blockedUntil;

    public Double getHourlyUsagePercentage() {
        if (hourlyLimit == null || hourlyLimit == 0) return 0.0;
        return (double) hourlyUsed / hourlyLimit * 100;
    }

    public Double getBurstUsagePercentage() {
        if (burstLimit == null || burstLimit == 0) return 0.0;
        return (double) burstUsed / burstLimit * 100;
    }
}

