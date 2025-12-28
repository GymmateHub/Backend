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
public class RateLimitStatistics {
    private UUID gymId;
    private Integer totalRequests;
    private Integer totalBlocks;
    private Integer rateLimitHits;
    private LocalDateTime since;

    public Double getBlockRate() {
        if (totalRequests == null || totalRequests == 0) return 0.0;
        return (double) totalBlocks / totalRequests * 100;
    }
}

