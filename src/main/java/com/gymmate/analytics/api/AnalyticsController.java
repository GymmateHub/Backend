package com.gymmate.analytics.api;

import com.gymmate.analytics.api.dto.*;
import com.gymmate.analytics.application.AnalyticsService;
import com.gymmate.analytics.domain.AnalyticsPeriod;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for analytics and KPI dashboard endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics and KPI Dashboard APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ===== MAIN DASHBOARD =====

    @GetMapping("/dashboard/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get complete dashboard with all KPIs for a gym")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        log.info("Fetching dashboard for gym: {} period: {}", gymId, period);
        DashboardResponse dashboard = analyticsService.getDashboard(gymId, period);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/dashboard/gym/{gymId}/today")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get today's dashboard snapshot")
    public ResponseEntity<ApiResponse<DashboardResponse>> getTodaysDashboard(@PathVariable UUID gymId) {
        DashboardResponse dashboard = analyticsService.getDashboard(gymId, AnalyticsPeriod.TODAY);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/dashboard/gym/{gymId}/weekly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get weekly dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getWeeklyDashboard(@PathVariable UUID gymId) {
        DashboardResponse dashboard = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_7_DAYS);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/dashboard/gym/{gymId}/monthly")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get monthly dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getMonthlyDashboard(@PathVariable UUID gymId) {
        DashboardResponse dashboard = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_30_DAYS);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ===== MEMBER ANALYTICS =====

    @GetMapping("/members/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get detailed member analytics")
    public ResponseEntity<ApiResponse<MemberAnalyticsResponse>> getMemberAnalytics(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        log.info("Fetching member analytics for gym: {} period: {}", gymId, period);
        MemberAnalyticsResponse analytics = analyticsService.getMemberAnalytics(gymId, period);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ===== REVENUE ANALYTICS =====

    @GetMapping("/revenue/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get detailed revenue analytics")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenueAnalytics(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        log.info("Fetching revenue analytics for gym: {} period: {}", gymId, period);
        RevenueAnalyticsResponse analytics = analyticsService.getRevenueAnalytics(gymId, period);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ===== CLASS ANALYTICS =====

    @GetMapping("/classes/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get detailed class and booking analytics")
    public ResponseEntity<ApiResponse<ClassAnalyticsResponse>> getClassAnalytics(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        log.info("Fetching class analytics for gym: {} period: {}", gymId, period);
        ClassAnalyticsResponse analytics = analyticsService.getClassAnalytics(gymId, period);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }

    // ===== KPI WIDGETS (Individual) =====

    @GetMapping("/kpi/members/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get member-related KPI cards only")
    public ResponseEntity<ApiResponse<MemberKpisResponse>> getMemberKpis(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        DashboardResponse full = analyticsService.getDashboard(gymId, period);
        MemberKpisResponse kpis = new MemberKpisResponse(
                full.totalMembers(),
                full.activeMembers(),
                full.newMembersThisMonth(),
                full.memberRetentionRate());
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    @GetMapping("/kpi/revenue/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get revenue-related KPI cards only")
    public ResponseEntity<ApiResponse<RevenueKpisResponse>> getRevenueKpis(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        DashboardResponse full = analyticsService.getDashboard(gymId, period);
        RevenueKpisResponse kpis = new RevenueKpisResponse(
                full.totalRevenue(),
                full.recurringRevenue(),
                full.posRevenue(),
                full.averageRevenuePerMember());
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    @GetMapping("/kpi/classes/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get class-related KPI cards only")
    public ResponseEntity<ApiResponse<ClassKpisResponse>> getClassKpis(
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "LAST_30_DAYS") AnalyticsPeriod period) {

        DashboardResponse full = analyticsService.getDashboard(gymId, period);
        ClassKpisResponse kpis = new ClassKpisResponse(
                full.classesToday(),
                full.bookingsToday(),
                full.averageClassAttendance(),
                full.classCapacityUtilization());
        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    // ===== INNER RESPONSE CLASSES =====

    public record MemberKpisResponse(
            KpiCardResponse totalMembers,
            KpiCardResponse activeMembers,
            KpiCardResponse newMembers,
            KpiCardResponse retentionRate) {
    }

    public record RevenueKpisResponse(
            KpiCardResponse totalRevenue,
            KpiCardResponse recurringRevenue,
            KpiCardResponse posRevenue,
            KpiCardResponse avgRevenuePerMember) {
    }

    public record ClassKpisResponse(
            KpiCardResponse classesToday,
            KpiCardResponse bookingsToday,
            KpiCardResponse avgAttendance,
            KpiCardResponse capacityUtilization) {
    }
}
