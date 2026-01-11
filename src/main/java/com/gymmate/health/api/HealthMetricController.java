package com.gymmate.health.api;

import com.gymmate.health.api.dto.*;
import com.gymmate.health.application.HealthMetricService;
import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.Enums.MetricType;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for health metrics and body composition tracking.
 * Implements FR-015: Health Insights & Goals (Body Composition Tracking).
 */
@Slf4j
@RestController
@RequestMapping("/api/health-metrics")
@RequiredArgsConstructor
@Tag(name = "Health Metrics", description = "Health Metrics & Body Composition Tracking APIs")
public class HealthMetricController {

    private final HealthMetricService healthMetricService;

    /**
     * Record a health metric.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Record metric", description = "Record a new health metric for a member")
    public ResponseEntity<ApiResponse<HealthMetricResponse>> recordMetric(
            @RequestParam UUID gymId,
            @Valid @RequestBody RecordMetricRequest request,
            Authentication authentication) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID recordedByUserId = UUID.fromString(authentication.getName());

        HealthMetric metric = healthMetricService.recordMetric(
            organisationId,
            gymId,
            request.memberId(),
            request.metricType(),
            request.value(),
            request.unit(),
            request.notes(),
            recordedByUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HealthMetricResponse.from(metric), "Metric recorded successfully"));
    }

    /**
     * Get latest metric by type.
     */
    @GetMapping("/latest")
    @Operation(summary = "Get latest metric", description = "Get the latest metric of a specific type for a member")
    public ResponseEntity<ApiResponse<HealthMetricResponse>> getLatestMetric(
            @RequestParam UUID memberId,
            @RequestParam MetricType metricType) {

        HealthMetric metric = healthMetricService.getLatestMetric(memberId, metricType);
        if (metric == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No metrics found"));
        }
        return ResponseEntity.ok(ApiResponse.success(HealthMetricResponse.from(metric)));
    }

    /**
     * Get metric history by type.
     */
    @GetMapping("/history")
    @Operation(summary = "Get metric history", description = "Get metric history for a specific type")
    public ResponseEntity<ApiResponse<List<HealthMetricResponse>>> getMetricHistory(
            @RequestParam UUID memberId,
            @RequestParam MetricType metricType) {

        List<HealthMetric> metrics = healthMetricService.getMetricHistory(memberId, metricType);
        List<HealthMetricResponse> responses = metrics.stream()
            .map(HealthMetricResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get metric history by date range.
     */
    @GetMapping("/history/range")
    @Operation(summary = "Get metric history by date range", description = "Get metric history within a date range")
    public ResponseEntity<ApiResponse<List<HealthMetricResponse>>> getMetricHistoryByDateRange(
            @RequestParam UUID memberId,
            @RequestParam MetricType metricType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<HealthMetric> metrics = healthMetricService.getMetricHistoryByDateRange(
            memberId, metricType, startDate, endDate
        );
        List<HealthMetricResponse> responses = metrics.stream()
            .map(HealthMetricResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all metrics for a member.
     */
    @GetMapping("/all")
    @Operation(summary = "Get all metrics", description = "Get all health metrics for a member")
    public ResponseEntity<ApiResponse<List<HealthMetricResponse>>> getAllMetrics(@RequestParam UUID memberId) {
        List<HealthMetric> metrics = healthMetricService.getAllMetrics(memberId);
        List<HealthMetricResponse> responses = metrics.stream()
            .map(HealthMetricResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get latest body composition snapshot.
     */
    @GetMapping("/body-composition")
    @Operation(summary = "Get body composition", description = "Get latest body composition snapshot for a member")
    public ResponseEntity<ApiResponse<BodyCompositionResponse>> getBodyComposition(@RequestParam UUID memberId) {
        HealthMetricService.BodyCompositionSnapshot snapshot = healthMetricService.getLatestBodyComposition(memberId);
        return ResponseEntity.ok(ApiResponse.success(BodyCompositionResponse.from(snapshot)));
    }

    /**
     * Analyze metric trend.
     */
    @GetMapping("/trend")
    @Operation(summary = "Analyze metric trend", description = "Analyze trend for a specific metric over time")
    public ResponseEntity<ApiResponse<MetricTrendResponse>> analyzeMetricTrend(
            @RequestParam UUID memberId,
            @RequestParam MetricType metricType,
            @RequestParam(defaultValue = "30") int days) {

        HealthMetricService.MetricTrend trend = healthMetricService.analyzeMetricTrend(memberId, metricType, days);
        return ResponseEntity.ok(ApiResponse.success(MetricTrendResponse.from(trend)));
    }

    /**
     * Calculate and record BMI.
     */
    @PostMapping("/bmi")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Calculate BMI", description = "Calculate and record BMI from weight and height")
    public ResponseEntity<ApiResponse<HealthMetricResponse>> calculateBMI(
            @RequestParam UUID gymId,
            @RequestParam UUID memberId,
            @RequestParam BigDecimal weightKg,
            @RequestParam BigDecimal heightM,
            Authentication authentication) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID recordedByUserId = UUID.fromString(authentication.getName());

        HealthMetric metric = healthMetricService.calculateAndRecordBMI(
            organisationId, gymId, memberId, weightKg, heightM, recordedByUserId
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(HealthMetricResponse.from(metric), "BMI calculated and recorded successfully"));
    }

    /**
     * Delete a health metric.
     */
    @DeleteMapping("/{metricId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Delete metric", description = "Delete a health metric")
    public ResponseEntity<ApiResponse<Void>> deleteMetric(@PathVariable UUID metricId) {
        healthMetricService.deleteMetric(metricId);
        return ResponseEntity.ok(ApiResponse.success(null, "Metric deleted successfully"));
    }
}

