package com.gymmate.analytics.api.dto;

import com.gymmate.analytics.domain.CategoryBreakdown;
import com.gymmate.analytics.domain.TimeSeriesDataPoint;

import java.math.BigDecimal;
import java.util.List;

/**
 * Complete dashboard response with all KPIs and charts.
 */
public record DashboardResponse(
        // Main KPIs
        KpiCardResponse totalMembers,
        KpiCardResponse activeMembers,
        KpiCardResponse newMembersThisMonth,
        KpiCardResponse memberRetentionRate,

        KpiCardResponse totalRevenue,
        KpiCardResponse recurringRevenue,
        KpiCardResponse posRevenue,
        KpiCardResponse averageRevenuePerMember,

        KpiCardResponse classesToday,
        KpiCardResponse bookingsToday,
        KpiCardResponse averageClassAttendance,
        KpiCardResponse classCapacityUtilization,

        // Chart Data
        List<TimeSeriesDataPoint> revenueChart,
        List<TimeSeriesDataPoint> memberGrowthChart,
        List<TimeSeriesDataPoint> bookingsTrendChart,

        // Breakdowns
        List<CategoryBreakdown> revenueBySource,
        List<CategoryBreakdown> membersByPlan,
        List<CategoryBreakdown> bookingsByClass,

        // Additional metrics
        BigDecimal churnRate,
        long expiringMemberships,
        long overduePayments,
        long lowStockItems) {
}
