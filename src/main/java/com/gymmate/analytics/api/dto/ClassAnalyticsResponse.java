package com.gymmate.analytics.api.dto;

import com.gymmate.analytics.domain.CategoryBreakdown;
import com.gymmate.analytics.domain.TimeSeriesDataPoint;

import java.math.BigDecimal;
import java.util.List;

/**
 * Class and booking analytics response DTO.
 */
public record ClassAnalyticsResponse(
        // Counts
        long totalClasses,
        long totalScheduledSessions,
        long totalBookings,
        long completedBookings,
        long cancelledBookings,
        long noShows,

        // Metrics
        BigDecimal averageAttendanceRate,
        BigDecimal capacityUtilization,
        BigDecimal cancellationRate,
        BigDecimal noShowRate,

        // Trends
        List<TimeSeriesDataPoint> bookingsTrend,
        List<TimeSeriesDataPoint> attendanceTrend,

        // Breakdowns
        List<CategoryBreakdown> bookingsByClass,
        List<CategoryBreakdown> bookingsByDayOfWeek,
        List<CategoryBreakdown> bookingsByTimeSlot,

        // Top performers
        List<TopClassResponse> topClassesByBookings,
        List<TopClassResponse> topClassesByAttendance,
        List<TopTrainerResponse> topTrainersByBookings) {
    public record TopClassResponse(String className, String categoryName, long bookings, BigDecimal attendanceRate) {
    }

    public record TopTrainerResponse(String trainerName, long classesHeld, long totalBookings,
            BigDecimal avgAttendance) {
    }
}
