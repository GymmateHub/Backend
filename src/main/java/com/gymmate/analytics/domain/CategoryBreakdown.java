package com.gymmate.analytics.domain;

import java.math.BigDecimal;

/**
 * Record for category-based breakdown in analytics.
 */
public record CategoryBreakdown(
        String category,
        long count,
        BigDecimal value,
        BigDecimal percentage) {
}
