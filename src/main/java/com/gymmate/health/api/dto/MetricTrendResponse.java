package com.gymmate.health.api.dto;

import com.gymmate.health.application.HealthMetricService;
import com.gymmate.health.domain.Enums.MetricType;

import java.math.BigDecimal;

/**
 * Response DTO for metric trend analysis.
 */
public record MetricTrendResponse(
    MetricType metricType,
    HealthMetricService.TrendDirection direction,
    BigDecimal change,
    BigDecimal percentageChange,
    int dataPoints
) {
    public static MetricTrendResponse from(HealthMetricService.MetricTrend trend) {
        return new MetricTrendResponse(
            trend.metricType(),
            trend.direction(),
            trend.change(),
            trend.percentageChange(),
            trend.dataPoints()
        );
    }
}
