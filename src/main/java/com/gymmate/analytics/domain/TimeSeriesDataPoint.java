package com.gymmate.analytics.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record representing a data point for time series analytics.
 */
public record TimeSeriesDataPoint(
        LocalDate date,
        BigDecimal value,
        String label) {
}
