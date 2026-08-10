package com.gymmate.analytics.internal.domain;

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
