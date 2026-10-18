package com.gymmate.reporting.internal.application.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Revenue analytics response DTO.
 */
public record RevenueAnalyticsResponse(
        BigDecimal totalRevenue,
        BigDecimal membershipRevenue,
        BigDecimal posRevenue,
        BigDecimal otherRevenue,
        BigDecimal refundsIssued,
        BigDecimal netRevenue,

        BigDecimal averageTransactionValue,
        long transactionCount,

        BigDecimal previousPeriodRevenue,
        BigDecimal revenueGrowthPercentage,

        BigDecimal projectedMonthlyRevenue,
        BigDecimal averageRevenuePerMember,

        Map<String, BigDecimal> revenueByPaymentType,
        Map<String, BigDecimal> revenueByDay) {
}
