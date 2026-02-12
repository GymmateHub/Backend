package com.gymmate.analytics.api.dto;

import com.gymmate.analytics.domain.CategoryBreakdown;
import com.gymmate.analytics.domain.TimeSeriesDataPoint;

import java.math.BigDecimal;
import java.util.List;

/**
 * Member analytics response DTO.
 */
public record MemberAnalyticsResponse(
        // Counts
        long totalMembers,
        long activeMembers,
        long inactiveMembers,
        long suspendedMembers,

        // Growth
        long newMembersThisPeriod,
        long cancelledMembersThisPeriod,
        long netMemberGrowth,
        BigDecimal growthPercentage,

        // Retention
        BigDecimal retentionRate,
        BigDecimal churnRate,
        BigDecimal averageMemberTenure,

        // Engagement
        BigDecimal averageVisitsPerMember,
        BigDecimal averageClassesPerMember,

        // Charts & Breakdowns
        List<TimeSeriesDataPoint> memberGrowthTrend,
        List<CategoryBreakdown> membersByPlan,
        List<CategoryBreakdown> membersByStatus,
        List<CategoryBreakdown> membersByJoinMonth,

        // Alerts
        long expiringThisMonth,
        long expiringNextMonth) {
}
