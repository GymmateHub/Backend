package com.gymmate.health.internal.application.dto;

import com.gymmate.health.internal.application.HealthAnalyticsService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for health insights and recommendations.
 */
public record HealthInsightsResponse(
    MemberHealthDashboardResponse.MetricTrendsResponse trends,
    List<String> recommendations,
    List<String> warnings,
    LocalDateTime generatedAt
) {
    public static HealthInsightsResponse from(HealthAnalyticsService.HealthInsights insights) {
        return new HealthInsightsResponse(
            MemberHealthDashboardResponse.MetricTrendsResponse.from(insights.trends()),
            insights.recommendations(),
            insights.warnings(),
            insights.generatedAt()
        );
    }
}
