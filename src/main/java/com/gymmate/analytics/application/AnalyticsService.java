package com.gymmate.analytics.application;

import com.gymmate.analytics.api.dto.*;
import com.gymmate.analytics.domain.AnalyticsPeriod;
import com.gymmate.analytics.domain.CategoryBreakdown;
import com.gymmate.analytics.domain.TimeSeriesDataPoint;
import com.gymmate.classes.domain.BookingStatus;
import com.gymmate.classes.infrastructure.ClassBookingJpaRepository;
import com.gymmate.classes.infrastructure.ClassScheduleJpaRepository;
import com.gymmate.classes.infrastructure.GymClassJpaRepository;
import com.gymmate.inventory.infrastructure.InventoryItemJpaRepository;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.membership.infrastructure.MembershipPlanJpaRepository;
import com.gymmate.pos.infrastructure.SaleJpaRepository;
import com.gymmate.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Application service for analytics and KPI dashboard generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MemberRepository memberRepository;
    private final MemberMembershipJpaRepository membershipRepository;
    private final MembershipPlanJpaRepository membershipPlanRepository;
    private final ClassBookingJpaRepository classBookingRepository;
    private final ClassScheduleJpaRepository classScheduleRepository;
    private final GymClassJpaRepository gymClassRepository;
    private final InventoryItemJpaRepository inventoryItemRepository;
    private final SaleJpaRepository saleRepository;

    // ===== MAIN DASHBOARD =====

    /**
     * Get complete dashboard with all KPIs for a gym.
     */
    public DashboardResponse getDashboard(UUID gymId, AnalyticsPeriod period) {
        DateRange dateRange = getDateRange(period);
        DateRange previousRange = getPreviousDateRange(period);

        log.info("Generating dashboard for gym: {} period: {} ({} to {})",
                gymId, period, dateRange.start(), dateRange.end());

        // Member KPIs
        KpiCardResponse totalMembers = getTotalMembersKpi(gymId, dateRange, previousRange);
        KpiCardResponse activeMembers = getActiveMembersKpi(gymId, dateRange, previousRange);
        KpiCardResponse newMembersThisMonth = getNewMembersKpi(gymId, dateRange, previousRange);
        KpiCardResponse memberRetentionRate = getRetentionRateKpi(gymId, dateRange, previousRange);

        // Revenue KPIs
        KpiCardResponse totalRevenue = getTotalRevenueKpi(gymId, dateRange, previousRange);
        KpiCardResponse recurringRevenue = getRecurringRevenueKpi(gymId, dateRange, previousRange);
        KpiCardResponse posRevenue = getPosRevenueKpi(gymId, dateRange, previousRange);
        KpiCardResponse avgRevenuePerMember = getAvgRevenuePerMemberKpi(gymId, dateRange, previousRange);

        // Class KPIs
        KpiCardResponse classesToday = getClassesTodayKpi(gymId);
        KpiCardResponse bookingsToday = getBookingsTodayKpi(gymId);
        KpiCardResponse avgClassAttendance = getAvgClassAttendanceKpi(gymId, dateRange, previousRange);
        KpiCardResponse classCapacityUtilization = getCapacityUtilizationKpi(gymId, dateRange, previousRange);

        // Charts
        List<TimeSeriesDataPoint> revenueChart = getRevenueTimeSeries(gymId, dateRange);
        List<TimeSeriesDataPoint> memberGrowthChart = getMemberGrowthTimeSeries(gymId, dateRange);
        List<TimeSeriesDataPoint> bookingsTrendChart = getBookingsTimeSeries(gymId, dateRange);

        // Breakdowns
        List<CategoryBreakdown> revenueBySource = getRevenueBySourceBreakdown(gymId, dateRange);
        List<CategoryBreakdown> membersByPlan = getMembersByPlanBreakdown(gymId);
        List<CategoryBreakdown> bookingsByClass = getBookingsByClassBreakdown(gymId, dateRange);

        // Additional metrics
        BigDecimal churnRate = calculateChurnRate(gymId, dateRange);
        long expiringMemberships = countExpiringMemberships(gymId);
        long overduePayments = 0L; // TODO: Implement when payment tracking is ready
        long lowStockItems = countLowStockItems(gymId);

        return new DashboardResponse(
                totalMembers, activeMembers, newMembersThisMonth, memberRetentionRate,
                totalRevenue, recurringRevenue, posRevenue, avgRevenuePerMember,
                classesToday, bookingsToday, avgClassAttendance, classCapacityUtilization,
                revenueChart, memberGrowthChart, bookingsTrendChart,
                revenueBySource, membersByPlan, bookingsByClass,
                churnRate, expiringMemberships, overduePayments, lowStockItems);
    }

    // ===== MEMBER ANALYTICS =====

    /**
     * Get detailed member analytics for a gym.
     */
    public MemberAnalyticsResponse getMemberAnalytics(UUID gymId, AnalyticsPeriod period) {
        DateRange dateRange = getDateRange(period);
        DateRange previousRange = getPreviousDateRange(period);

        long totalMembers = countTotalMembers(gymId);
        long activeMembers = countActiveMembers(gymId);
        long inactiveMembers = totalMembers - activeMembers;
        long suspendedMembers = countSuspendedMembers(gymId);

        long newMembers = countNewMembers(gymId, dateRange);
        long cancelledMembers = countCancelledMembers(gymId, dateRange);
        long netGrowth = newMembers - cancelledMembers;

        long previousNewMembers = countNewMembers(gymId, previousRange);
        BigDecimal growthPercentage = previousNewMembers > 0
                ? BigDecimal.valueOf(newMembers - previousNewMembers)
                        .divide(BigDecimal.valueOf(previousNewMembers), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        BigDecimal retentionRate = calculateRetentionRate(gymId, dateRange);
        BigDecimal churnRate = BigDecimal.valueOf(100).subtract(retentionRate);
        BigDecimal avgTenure = calculateAverageMemberTenure(gymId);

        BigDecimal avgVisits = calculateAverageVisitsPerMember(gymId, dateRange);
        BigDecimal avgClasses = calculateAverageClassesPerMember(gymId, dateRange);

        List<TimeSeriesDataPoint> memberGrowthTrend = getMemberGrowthTimeSeries(gymId, dateRange);
        List<CategoryBreakdown> membersByPlan = getMembersByPlanBreakdown(gymId);
        List<CategoryBreakdown> membersByStatus = getMembersByStatusBreakdown(gymId);
        List<CategoryBreakdown> membersByJoinMonth = getMembersByJoinMonthBreakdown(gymId);

        long expiringThisMonth = countExpiringMemberships(gymId, 30);
        long expiringNextMonth = countExpiringMemberships(gymId, 60) - expiringThisMonth;

        return new MemberAnalyticsResponse(
                totalMembers, activeMembers, inactiveMembers, suspendedMembers,
                newMembers, cancelledMembers, netGrowth, growthPercentage,
                retentionRate, churnRate, avgTenure,
                avgVisits, avgClasses,
                memberGrowthTrend, membersByPlan, membersByStatus, membersByJoinMonth,
                expiringThisMonth, expiringNextMonth);
    }

    // ===== REVENUE ANALYTICS =====

    /**
     * Get detailed revenue analytics for a gym.
     */
    public RevenueAnalyticsResponse getRevenueAnalytics(UUID gymId, AnalyticsPeriod period) {
        DateRange dateRange = getDateRange(period);
        DateRange previousRange = getPreviousDateRange(period);

        BigDecimal posRevenue = getPosRevenue(gymId, dateRange);
        BigDecimal membershipRevenue = getMembershipRevenue(gymId, dateRange);
        BigDecimal otherRevenue = BigDecimal.ZERO; // Future expansion
        BigDecimal refunds = getRefunds(gymId, dateRange);
        BigDecimal totalRevenue = posRevenue.add(membershipRevenue).add(otherRevenue);
        BigDecimal netRevenue = totalRevenue.subtract(refunds);

        long transactionCount = getTransactionCount(gymId, dateRange);
        BigDecimal avgTransactionValue = transactionCount > 0
                ? totalRevenue.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal previousRevenue = getPosRevenue(gymId, previousRange)
                .add(getMembershipRevenue(gymId, previousRange));
        BigDecimal growthPercentage = previousRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalRevenue.subtract(previousRevenue)
                        .divide(previousRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.valueOf(100);

        // Projections
        long daysInPeriod = ChronoUnit.DAYS.between(dateRange.start(), dateRange.end()) + 1;
        BigDecimal dailyAverage = totalRevenue.divide(BigDecimal.valueOf(daysInPeriod), 2, RoundingMode.HALF_UP);
        BigDecimal projectedMonthly = dailyAverage.multiply(BigDecimal.valueOf(30));

        long activeMembers = countActiveMembers(gymId);
        BigDecimal avgRevenuePerMember = activeMembers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(activeMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, BigDecimal> revenueByPaymentType = getRevenueByPaymentType(gymId, dateRange);
        Map<String, BigDecimal> revenueByDay = getRevenueByDay(gymId, dateRange);

        return new RevenueAnalyticsResponse(
                totalRevenue, membershipRevenue, posRevenue, otherRevenue, refunds, netRevenue,
                avgTransactionValue, transactionCount,
                previousRevenue, growthPercentage,
                projectedMonthly, avgRevenuePerMember,
                revenueByPaymentType, revenueByDay);
    }

    // ===== CLASS ANALYTICS =====

    /**
     * Get detailed class and booking analytics for a gym.
     */
    public ClassAnalyticsResponse getClassAnalytics(UUID gymId, AnalyticsPeriod period) {
        DateRange dateRange = getDateRange(period);

        long totalClasses = countTotalClasses(gymId);
        long totalScheduled = countScheduledSessions(gymId, dateRange);
        long totalBookings = countTotalBookings(gymId, dateRange);
        long completedBookings = countCompletedBookings(gymId, dateRange);
        long cancelledBookings = countCancelledBookings(gymId, dateRange);
        long noShows = countNoShows(gymId, dateRange);

        BigDecimal avgAttendance = calculateAverageAttendance(gymId, dateRange);
        BigDecimal capacityUtilization = calculateCapacityUtilization(gymId, dateRange);
        BigDecimal cancellationRate = totalBookings > 0
                ? BigDecimal.valueOf(cancelledBookings)
                        .divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal noShowRate = totalBookings > 0
                ? BigDecimal.valueOf(noShows)
                        .divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<TimeSeriesDataPoint> bookingsTrend = getBookingsTimeSeries(gymId, dateRange);
        List<TimeSeriesDataPoint> attendanceTrend = getAttendanceTimeSeries(gymId, dateRange);

        List<CategoryBreakdown> bookingsByClass = getBookingsByClassBreakdown(gymId, dateRange);
        List<CategoryBreakdown> bookingsByDay = getBookingsByDayOfWeekBreakdown(gymId, dateRange);
        List<CategoryBreakdown> bookingsByTimeSlot = getBookingsByTimeSlotBreakdown(gymId, dateRange);

        List<ClassAnalyticsResponse.TopClassResponse> topByBookings = getTopClassesByBookings(gymId, dateRange);
        List<ClassAnalyticsResponse.TopClassResponse> topByAttendance = getTopClassesByAttendance(gymId, dateRange);
        List<ClassAnalyticsResponse.TopTrainerResponse> topTrainers = getTopTrainersByBookings(gymId, dateRange);

        return new ClassAnalyticsResponse(
                totalClasses, totalScheduled, totalBookings, completedBookings, cancelledBookings, noShows,
                avgAttendance, capacityUtilization, cancellationRate, noShowRate,
                bookingsTrend, attendanceTrend,
                bookingsByClass, bookingsByDay, bookingsByTimeSlot,
                topByBookings, topByAttendance, topTrainers);
    }

    // ===== HELPER METHODS - DATE RANGES =====

    private DateRange getDateRange(AnalyticsPeriod period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case TODAY -> new DateRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            case YESTERDAY -> new DateRange(today.minusDays(1).atStartOfDay(), today.atStartOfDay());
            case LAST_7_DAYS -> new DateRange(today.minusDays(7).atStartOfDay(), today.plusDays(1).atStartOfDay());
            case LAST_30_DAYS, THIS_MONTH ->
                new DateRange(today.minusDays(30).atStartOfDay(), today.plusDays(1).atStartOfDay());
            case THIS_WEEK -> new DateRange(today.minusDays(today.getDayOfWeek().getValue() - 1).atStartOfDay(),
                    today.plusDays(1).atStartOfDay());
            case LAST_MONTH -> new DateRange(today.minusMonths(1).withDayOfMonth(1).atStartOfDay(),
                    today.withDayOfMonth(1).atStartOfDay());
            case THIS_QUARTER -> new DateRange(today.minusMonths(3).atStartOfDay(), today.plusDays(1).atStartOfDay());
            case THIS_YEAR -> new DateRange(today.withDayOfYear(1).atStartOfDay(), today.plusDays(1).atStartOfDay());
            case LAST_YEAR -> new DateRange(today.minusYears(1).withDayOfYear(1).atStartOfDay(),
                    today.withDayOfYear(1).atStartOfDay());
            default -> new DateRange(today.minusDays(30).atStartOfDay(), today.plusDays(1).atStartOfDay());
        };
    }

    private DateRange getPreviousDateRange(AnalyticsPeriod period) {
        DateRange current = getDateRange(period);
        long days = ChronoUnit.DAYS.between(current.start(), current.end());
        return new DateRange(current.start().minusDays(days), current.start());
    }

    // ===== HELPER METHODS - KPI CARDS =====

    private KpiCardResponse getTotalMembersKpi(UUID gymId, DateRange current, DateRange previous) {
        long currentCount = countTotalMembers(gymId);
        long previousCount = countTotalMembersAsOf(gymId, previous.end());
        return KpiCardResponse.of("Total Members", currentCount, previousCount, "users", "blue");
    }

    private KpiCardResponse getActiveMembersKpi(UUID gymId, DateRange current, DateRange previous) {
        long currentCount = countActiveMembers(gymId);
        long previousCount = countActiveMembersAsOf(gymId, previous.end());
        return KpiCardResponse.of("Active Members", currentCount, previousCount, "user-check", "green");
    }

    private KpiCardResponse getNewMembersKpi(UUID gymId, DateRange current, DateRange previous) {
        long currentCount = countNewMembers(gymId, current);
        long previousCount = countNewMembers(gymId, previous);
        return KpiCardResponse.of("New Members", currentCount, previousCount, "user-plus", "purple");
    }

    private KpiCardResponse getRetentionRateKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentRate = calculateRetentionRate(gymId, current);
        BigDecimal previousRate = calculateRetentionRate(gymId, previous);
        return KpiCardResponse.ofPercentage("Retention Rate", currentRate, previousRate, "refresh", "teal");
    }

    private KpiCardResponse getTotalRevenueKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentRevenue = getPosRevenue(gymId, current).add(getMembershipRevenue(gymId, current));
        BigDecimal previousRevenue = getPosRevenue(gymId, previous).add(getMembershipRevenue(gymId, previous));
        return KpiCardResponse.ofMoney("Total Revenue", currentRevenue, previousRevenue, "dollar-sign", "green");
    }

    private KpiCardResponse getRecurringRevenueKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentRevenue = getMembershipRevenue(gymId, current);
        BigDecimal previousRevenue = getMembershipRevenue(gymId, previous);
        return KpiCardResponse.ofMoney("Recurring Revenue", currentRevenue, previousRevenue, "repeat", "blue");
    }

    private KpiCardResponse getPosRevenueKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentRevenue = getPosRevenue(gymId, current);
        BigDecimal previousRevenue = getPosRevenue(gymId, previous);
        return KpiCardResponse.ofMoney("POS Revenue", currentRevenue, previousRevenue, "shopping-cart", "orange");
    }

    private KpiCardResponse getAvgRevenuePerMemberKpi(UUID gymId, DateRange current, DateRange previous) {
        long activeMembers = countActiveMembers(gymId);
        BigDecimal totalRevenue = getPosRevenue(gymId, current).add(getMembershipRevenue(gymId, current));
        BigDecimal currentArpm = activeMembers > 0
                ? totalRevenue.divide(BigDecimal.valueOf(activeMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long prevActiveMembers = countActiveMembersAsOf(gymId, previous.end());
        BigDecimal prevTotalRevenue = getPosRevenue(gymId, previous).add(getMembershipRevenue(gymId, previous));
        BigDecimal previousArpm = prevActiveMembers > 0
                ? prevTotalRevenue.divide(BigDecimal.valueOf(prevActiveMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return KpiCardResponse.ofMoney("Avg Revenue/Member", currentArpm, previousArpm, "trending-up", "purple");
    }

    private KpiCardResponse getClassesTodayKpi(UUID gymId) {
        long todayClasses = countScheduledSessions(gymId, getDateRange(AnalyticsPeriod.TODAY));
        long yesterdayClasses = countScheduledSessions(gymId, getDateRange(AnalyticsPeriod.YESTERDAY));
        return KpiCardResponse.of("Classes Today", todayClasses, yesterdayClasses, "calendar", "indigo");
    }

    private KpiCardResponse getBookingsTodayKpi(UUID gymId) {
        long todayBookings = countTotalBookings(gymId, getDateRange(AnalyticsPeriod.TODAY));
        long yesterdayBookings = countTotalBookings(gymId, getDateRange(AnalyticsPeriod.YESTERDAY));
        return KpiCardResponse.of("Bookings Today", todayBookings, yesterdayBookings, "check-circle", "cyan");
    }

    private KpiCardResponse getAvgClassAttendanceKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentAttendance = calculateAverageAttendance(gymId, current);
        BigDecimal previousAttendance = calculateAverageAttendance(gymId, previous);
        return KpiCardResponse.ofPercentage("Avg Attendance", currentAttendance, previousAttendance, "users", "teal");
    }

    private KpiCardResponse getCapacityUtilizationKpi(UUID gymId, DateRange current, DateRange previous) {
        BigDecimal currentUtil = calculateCapacityUtilization(gymId, current);
        BigDecimal previousUtil = calculateCapacityUtilization(gymId, previous);
        return KpiCardResponse.ofPercentage("Capacity Utilization", currentUtil, previousUtil, "pie-chart", "amber");
    }

    // ===== HELPER METHODS - DATA QUERIES =====
    // NOTE: These methods contain placeholder implementations that should be
    // replaced
    // with actual repository queries when the complete schema is available

    private long countTotalMembers(UUID gymId) {
        try {
            return memberRepository.countByGymId(gymId);
        } catch (Exception e) {
            log.warn("Could not count members: {}", e.getMessage());
            return 0;
        }
    }

    private long countTotalMembersAsOf(UUID gymId, LocalDateTime asOf) {
        // Simplified: In production, query members where createdAt <= asOf
        return countTotalMembers(gymId);
    }

    private long countActiveMembers(UUID gymId) {
        try {
            return membershipRepository.countActiveByGymId(gymId);
        } catch (Exception e) {
            log.warn("Could not count active members: {}", e.getMessage());
            return 0;
        }
    }

    private long countActiveMembersAsOf(UUID gymId, LocalDateTime asOf) {
        return countActiveMembers(gymId);
    }

    private long countSuspendedMembers(UUID gymId) {
        try {
            return membershipRepository.countByGymIdAndStatus(gymId, MembershipStatus.PAUSED);
        } catch (Exception e) {
            log.warn("Could not count suspended members: {}", e.getMessage());
            return 0;
        }
    }

    private long countNewMembers(UUID gymId, DateRange range) {
        try {
            return memberRepository.countByGymIdAndCreatedAtBetween(gymId, range.start(), range.end());
        } catch (Exception e) {
            return 0;
        }
    }

    private long countCancelledMembers(UUID gymId, DateRange range) {
        try {
            return membershipRepository.countCancelledByGymIdAndDateRange(gymId, range.start(), range.end());
        } catch (Exception e) {
            log.warn("Could not count cancelled members: {}", e.getMessage());
            return 0;
        }
    }

    private BigDecimal calculateRetentionRate(UUID gymId, DateRange range) {
        // Simplified retention calculation
        long startMembers = countActiveMembersAsOf(gymId, range.start());
        long endMembers = countActiveMembers(gymId);
        if (startMembers == 0)
            return BigDecimal.valueOf(100);
        return BigDecimal.valueOf(endMembers)
                .divide(BigDecimal.valueOf(startMembers), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .min(BigDecimal.valueOf(100));
    }

    private BigDecimal calculateChurnRate(UUID gymId, DateRange range) {
        return BigDecimal.valueOf(100).subtract(calculateRetentionRate(gymId, range));
    }

    private BigDecimal calculateAverageMemberTenure(UUID gymId) {
        return BigDecimal.valueOf(180); // Placeholder: 6 months average
    }

    private BigDecimal calculateAverageVisitsPerMember(UUID gymId, DateRange range) {
        return BigDecimal.valueOf(8); // Placeholder
    }

    private BigDecimal calculateAverageClassesPerMember(UUID gymId, DateRange range) {
        long totalBookings = countTotalBookings(gymId, range);
        long activeMembers = countActiveMembers(gymId);
        if (activeMembers == 0)
            return BigDecimal.ZERO;
        return BigDecimal.valueOf(totalBookings).divide(BigDecimal.valueOf(activeMembers), 2, RoundingMode.HALF_UP);
    }

    private long countExpiringMemberships(UUID gymId) {
        return countExpiringMemberships(gymId, 30);
    }

    private long countExpiringMemberships(UUID gymId, int days) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return membershipRepository.findExpiringMemberships(gymId, now, now.plusDays(days)).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private long countLowStockItems(UUID gymId) {
        try {
            return inventoryItemRepository.countByGymIdAndCurrentStockLessThanMinimumStock(gymId);
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal getPosRevenue(UUID gymId, DateRange range) {
        try {
            var sumTotal = saleRepository
              .sumTotalByGymIdAndDateRange(gymId, range.start(), range.end());

            if (sumTotal == null)
              return BigDecimal.ZERO;

            return sumTotal;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getMembershipRevenue(UUID gymId, DateRange range) {
        try {
            BigDecimal revenue = membershipRepository.sumProjectedRevenueByGymIdAndDateRange(
                    gymId, range.start(), range.end());
            return revenue != null ? revenue : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Could not calculate membership revenue: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getRefunds(UUID gymId, DateRange range) {
        return BigDecimal.ZERO; // Placeholder
    }

    private long getTransactionCount(UUID gymId, DateRange range) {
        try {
            return saleRepository.findByGymIdAndDateRange(gymId, range.start(), range.end()).size();
        } catch (Exception e) {
            return 0;
        }
    }

    private Map<String, BigDecimal> getRevenueByPaymentType(UUID gymId, DateRange range) {
        return new HashMap<>(); // Placeholder
    }

    private Map<String, BigDecimal> getRevenueByDay(UUID gymId, DateRange range) {
        return new HashMap<>(); // Placeholder
    }

    private long countTotalClasses(UUID gymId) {
        try {
            return gymClassRepository.countByGymId(gymId);
        } catch (Exception e) {
            return 0;
        }
    }

    private long countScheduledSessions(UUID gymId, DateRange range) {
        try {
            return classScheduleRepository.countByGymIdAndStartTimeBetween(gymId, range.start(), range.end());
        } catch (Exception e) {
            return 0;
        }
    }

    private long countTotalBookings(UUID gymId, DateRange range) {
        try {
            return classBookingRepository.countByGymIdAndDateRange(gymId, range.start(), range.end());
        } catch (Exception e) {
            log.warn("Could not count bookings: {}", e.getMessage());
            return 0;
        }
    }

    private long countCompletedBookings(UUID gymId, DateRange range) {
        try {
            return classBookingRepository.countByGymIdAndStatusAndDateRange(
                    gymId, BookingStatus.COMPLETED, range.start(), range.end());
        } catch (Exception e) {
            log.warn("Could not count completed bookings: {}", e.getMessage());
            return 0;
        }
    }

    private long countCancelledBookings(UUID gymId, DateRange range) {
        try {
            return classBookingRepository.countByGymIdAndStatusAndDateRange(
                    gymId, BookingStatus.CANCELLED, range.start(), range.end());
        } catch (Exception e) {
            log.warn("Could not count cancelled bookings: {}", e.getMessage());
            return 0;
        }
    }

    private long countNoShows(UUID gymId, DateRange range) {
        try {
            return classBookingRepository.countByGymIdAndStatusAndDateRange(
                    gymId, BookingStatus.NO_SHOW, range.start(), range.end());
        } catch (Exception e) {
            log.warn("Could not count no-shows: {}", e.getMessage());
            return 0;
        }
    }

    private BigDecimal calculateAverageAttendance(UUID gymId, DateRange range) {
        try {
            long totalBookings = countTotalBookings(gymId, range);
            long completedBookings = countCompletedBookings(gymId, range);
            if (totalBookings == 0)
                return BigDecimal.ZERO;
            return BigDecimal.valueOf(completedBookings)
                    .divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } catch (Exception e) {
            log.warn("Could not calculate average attendance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateCapacityUtilization(UUID gymId, DateRange range) {
        try {
            long scheduledSessions = countScheduledSessions(gymId, range);
            long totalBookings = countTotalBookings(gymId, range);
            // Estimate average capacity of 15 per class
            long totalCapacity = scheduledSessions * 15;
            if (totalCapacity == 0)
                return BigDecimal.ZERO;
            return BigDecimal.valueOf(totalBookings)
                    .divide(BigDecimal.valueOf(totalCapacity), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .min(BigDecimal.valueOf(100));
        } catch (Exception e) {
            log.warn("Could not calculate capacity utilization: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // ===== TIME SERIES DATA =====

    private List<TimeSeriesDataPoint> getRevenueTimeSeries(UUID gymId, DateRange range) {
        List<TimeSeriesDataPoint> data = new ArrayList<>();
        LocalDate current = range.start().toLocalDate();
        LocalDate end = range.end().toLocalDate();

        while (!current.isAfter(end)) {
            DateRange dayRange = new DateRange(current.atStartOfDay(), current.plusDays(1).atStartOfDay());
            BigDecimal dayRevenue = getPosRevenue(gymId, dayRange);
            data.add(new TimeSeriesDataPoint(current, dayRevenue, current.toString()));
            current = current.plusDays(1);
        }
        return data;
    }

    private List<TimeSeriesDataPoint> getMemberGrowthTimeSeries(UUID gymId, DateRange range) {
        List<TimeSeriesDataPoint> data = new ArrayList<>();
        LocalDate current = range.start().toLocalDate();
        LocalDate end = range.end().toLocalDate();
        long runningTotal = countTotalMembersAsOf(gymId, range.start());

        while (!current.isAfter(end)) {
            DateRange dayRange = new DateRange(current.atStartOfDay(), current.plusDays(1).atStartOfDay());
            long newMembers = countNewMembers(gymId, dayRange);
            runningTotal += newMembers;
            data.add(new TimeSeriesDataPoint(current, BigDecimal.valueOf(runningTotal), current.toString()));
            current = current.plusDays(1);
        }
        return data;
    }

    private List<TimeSeriesDataPoint> getBookingsTimeSeries(UUID gymId, DateRange range) {
        List<TimeSeriesDataPoint> data = new ArrayList<>();
        LocalDate current = range.start().toLocalDate();
        LocalDate end = range.end().toLocalDate();

        while (!current.isAfter(end)) {
            DateRange dayRange = new DateRange(current.atStartOfDay(), current.plusDays(1).atStartOfDay());
            long bookings = countTotalBookings(gymId, dayRange);
            data.add(new TimeSeriesDataPoint(current, BigDecimal.valueOf(bookings), current.toString()));
            current = current.plusDays(1);
        }
        return data;
    }

    private List<TimeSeriesDataPoint> getAttendanceTimeSeries(UUID gymId, DateRange range) {
        return getBookingsTimeSeries(gymId, range); // Simplified
    }

    // ===== CATEGORY BREAKDOWNS =====

    private List<CategoryBreakdown> getRevenueBySourceBreakdown(UUID gymId, DateRange range) {
        BigDecimal posRevenue = getPosRevenue(gymId, range);
        BigDecimal membershipRevenue = getMembershipRevenue(gymId, range);
        BigDecimal total = posRevenue.add(membershipRevenue);

        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        return List.of(
                new CategoryBreakdown("POS Sales", 0, posRevenue,
                        posRevenue.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))),
                new CategoryBreakdown("Memberships", 0, membershipRevenue,
                        membershipRevenue.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))));
    }

    private List<CategoryBreakdown> getMembersByPlanBreakdown(UUID gymId) {
        try {
            List<Object[]> planCounts = membershipRepository.countActiveMembersByPlan(gymId);
            if (planCounts.isEmpty())
                return List.of();

            long total = planCounts.stream()
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();

            if (total == 0)
                return List.of();

            return planCounts.stream()
                    .map(row -> {
                        String planName = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        BigDecimal percentage = BigDecimal.valueOf(count * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP);
                        return new CategoryBreakdown(planName, count, BigDecimal.ZERO, percentage);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Could not get members by plan breakdown: {}", e.getMessage());
            return List.of();
        }
    }

    private List<CategoryBreakdown> getMembersByStatusBreakdown(UUID gymId) {
        long active = countActiveMembers(gymId);
        long total = countTotalMembers(gymId);
        long inactive = total - active;

        if (total == 0)
            return List.of();

        return List.of(
                new CategoryBreakdown("Active", active, BigDecimal.ZERO,
                        BigDecimal.valueOf(active * 100.0 / total).setScale(1, RoundingMode.HALF_UP)),
                new CategoryBreakdown("Inactive", inactive, BigDecimal.ZERO,
                        BigDecimal.valueOf(inactive * 100.0 / total).setScale(1, RoundingMode.HALF_UP)));
    }

    private List<CategoryBreakdown> getMembersByJoinMonthBreakdown(UUID gymId) {
        return List.of(); // Placeholder
    }

    private List<CategoryBreakdown> getBookingsByClassBreakdown(UUID gymId, DateRange range) {
        try {
            List<Object[]> classCounts = classBookingRepository.countBookingsByClassForGym(
                    gymId, range.start(), range.end());
            if (classCounts.isEmpty())
                return List.of();

            long total = classCounts.stream()
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();

            if (total == 0)
                return List.of();

            return classCounts.stream()
                    .limit(10) // Top 10 classes
                    .map(row -> {
                        String className = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        BigDecimal percentage = BigDecimal.valueOf(count * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP);
                        return new CategoryBreakdown(className, count, BigDecimal.ZERO, percentage);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Could not get bookings by class breakdown: {}", e.getMessage());
            return List.of();
        }
    }

    private List<CategoryBreakdown> getBookingsByDayOfWeekBreakdown(UUID gymId, DateRange range) {
        try {
            List<Object[]> dayCounts = classBookingRepository.countBookingsByDayOfWeek(
                    gymId, range.start(), range.end());
            if (dayCounts.isEmpty())
                return List.of();

            String[] dayNames = { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
            long total = dayCounts.stream()
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();

            if (total == 0)
                return List.of();

            return dayCounts.stream()
                    .map(row -> {
                        int dayOfWeek = ((Number) row[0]).intValue();
                        String dayName = dayOfWeek > 0 && dayOfWeek < dayNames.length ? dayNames[dayOfWeek] : "Unknown";
                        long count = ((Number) row[1]).longValue();
                        BigDecimal percentage = BigDecimal.valueOf(count * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP);
                        return new CategoryBreakdown(dayName, count, BigDecimal.ZERO, percentage);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Could not get bookings by day of week breakdown: {}", e.getMessage());
            return List.of();
        }
    }

    private List<CategoryBreakdown> getBookingsByTimeSlotBreakdown(UUID gymId, DateRange range) {
        try {
            List<Object[]> timeCounts = classBookingRepository.countBookingsByTimeSlot(
                    gymId, range.start(), range.end());
            if (timeCounts.isEmpty())
                return List.of();

            long total = timeCounts.stream()
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();

            if (total == 0)
                return List.of();

            return timeCounts.stream()
                    .map(row -> {
                        int hour = ((Number) row[0]).intValue();
                        String timeSlot = String.format("%02d:00-%02d:00", hour, (hour + 1) % 24);
                        long count = ((Number) row[1]).longValue();
                        BigDecimal percentage = BigDecimal.valueOf(count * 100.0 / total)
                                .setScale(1, RoundingMode.HALF_UP);
                        return new CategoryBreakdown(timeSlot, count, BigDecimal.ZERO, percentage);
                    })
                    .toList();
        } catch (Exception e) {
            log.warn("Could not get bookings by time slot breakdown: {}", e.getMessage());
            return List.of();
        }
    }

    // ===== TOP PERFORMERS =====

    private List<ClassAnalyticsResponse.TopClassResponse> getTopClassesByBookings(UUID gymId, DateRange range) {
        try {
            List<Object[]> classCounts = classBookingRepository.countBookingsByClassForGym(
                    gymId, range.start(), range.end());
            if (classCounts.isEmpty())
                return List.of();

            return classCounts.stream()
                    .limit(5)
                    .map(row -> new ClassAnalyticsResponse.TopClassResponse(
                            (String) row[0], // className
                            "N/A", // categoryName - not available in current query
                            ((Number) row[1]).longValue(), // bookings
                            BigDecimal.ZERO // attendanceRate - requires more data
                    ))
                    .toList();
        } catch (Exception e) {
            log.warn("Could not get top classes by bookings: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ClassAnalyticsResponse.TopClassResponse> getTopClassesByAttendance(UUID gymId, DateRange range) {
        // For now, use the same as bookings since we track completed bookings as
        // attendance
        return getTopClassesByBookings(gymId, range);
    }

    private List<ClassAnalyticsResponse.TopTrainerResponse> getTopTrainersByBookings(UUID gymId, DateRange range) {
        // This would require a join with trainers - for now return empty
        // Future: query class_schedules -> trainers and count bookings per trainer
        return List.of();
    }

    // ===== INNER CLASSES =====

    private record DateRange(LocalDateTime start, LocalDateTime end) {
    }
}
