package com.gymmate.inventory.api;

import com.gymmate.inventory.api.dto.*;
import com.gymmate.inventory.application.MaintenanceService;
import com.gymmate.inventory.domain.MaintenanceRecord;
import com.gymmate.inventory.domain.MaintenanceSchedule;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for maintenance management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Equipment Maintenance Management APIs")
public class MaintenanceController {

  private final MaintenanceService maintenanceService;

  // ===== Maintenance Records =====

  @PostMapping("/records")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create maintenance record")
  public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> createMaintenanceRecord(
      @Valid @RequestBody MaintenanceRecordCreateRequest request) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    UUID gymId = TenantContext.getCurrentGymId();
    
    MaintenanceRecord record = MaintenanceRecord.builder()
      .equipmentId(request.equipmentId())
      .maintenanceDate(request.maintenanceDate())
      .maintenanceType(request.maintenanceType())
      .description(request.description())
      .performedBy(request.performedBy())
      .technicianCompany(request.technicianCompany())
      .cost(request.cost())
      .partsReplaced(request.partsReplaced())
      .nextMaintenanceDue(request.nextMaintenanceDue())
      .notes(request.notes())
      .invoiceNumber(request.invoiceNumber())
      .invoiceUrl(request.invoiceUrl())
      .completed(request.completed())
      .completionNotes(request.completionNotes())
      .build();
    
    record.setOrganisationId(organisationId);
    record.setGymId(gymId);
    
    MaintenanceRecord created = maintenanceService.createMaintenanceRecord(record);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(MaintenanceRecordResponse.fromEntity(created), 
        "Maintenance record created successfully"));
  }

  @GetMapping("/records/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get maintenance record by ID")
  public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> getMaintenanceRecord(@PathVariable UUID id) {
    MaintenanceRecord record = maintenanceService.getMaintenanceRecordById(id);
    return ResponseEntity.ok(ApiResponse.success(MaintenanceRecordResponse.fromEntity(record)));
  }

  @GetMapping("/records/equipment/{equipmentId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get maintenance records for equipment")
  public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getMaintenanceRecordsByEquipment(
      @PathVariable UUID equipmentId) {
    List<MaintenanceRecord> records = maintenanceService.getMaintenanceRecordsByEquipment(equipmentId);
    List<MaintenanceRecordResponse> responses = records.stream()
      .map(MaintenanceRecordResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/records/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get maintenance records for gym")
  public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getMaintenanceRecordsByGym(
      @PathVariable UUID gymId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
    
    List<MaintenanceRecord> records;
    if (startDate != null && endDate != null) {
      records = maintenanceService.getMaintenanceRecordsByGymAndDateRange(gymId, startDate, endDate);
    } else {
      records = maintenanceService.getMaintenanceRecordsByGym(gymId);
    }
    
    List<MaintenanceRecordResponse> responses = records.stream()
      .map(MaintenanceRecordResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PutMapping("/records/{id}/complete")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Complete maintenance record")
  public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> completeMaintenanceRecord(
      @PathVariable UUID id,
      @RequestParam(required = false) String completionNotes) {
    MaintenanceRecord record = maintenanceService.completeMaintenanceRecord(id, completionNotes);
    return ResponseEntity.ok(ApiResponse.success(MaintenanceRecordResponse.fromEntity(record), 
      "Maintenance completed successfully"));
  }

  // ===== Maintenance Schedules =====

  @PostMapping("/schedules")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create maintenance schedule")
  public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> createMaintenanceSchedule(
      @Valid @RequestBody MaintenanceScheduleCreateRequest request) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    UUID gymId = TenantContext.getCurrentGymId();
    
    MaintenanceSchedule schedule = MaintenanceSchedule.builder()
      .equipmentId(request.equipmentId())
      .scheduleName(request.scheduleName())
      .description(request.description())
      .scheduledDate(request.scheduledDate())
      .maintenanceType(request.maintenanceType())
      .assignedTo(request.assignedTo())
      .estimatedDurationHours(request.estimatedDurationHours())
      .recurring(request.recurring())
      .recurrenceIntervalDays(request.recurrenceIntervalDays())
      .notes(request.notes())
      .build();
    
    schedule.setOrganisationId(organisationId);
    schedule.setGymId(gymId);
    
    MaintenanceSchedule created = maintenanceService.createMaintenanceSchedule(schedule);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(MaintenanceScheduleResponse.fromEntity(created), 
        "Maintenance schedule created successfully"));
  }

  @GetMapping("/schedules/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get maintenance schedule by ID")
  public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> getMaintenanceSchedule(@PathVariable UUID id) {
    MaintenanceSchedule schedule = maintenanceService.getMaintenanceScheduleById(id);
    return ResponseEntity.ok(ApiResponse.success(MaintenanceScheduleResponse.fromEntity(schedule)));
  }

  @GetMapping("/schedules/gym/{gymId}/pending")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get pending maintenance schedules for gym")
  public ResponseEntity<ApiResponse<List<MaintenanceScheduleResponse>>> getPendingMaintenanceSchedules(
      @PathVariable UUID gymId) {
    List<MaintenanceSchedule> schedules = maintenanceService.getPendingMaintenanceSchedulesByGym(gymId);
    List<MaintenanceScheduleResponse> responses = schedules.stream()
      .map(MaintenanceScheduleResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/schedules/gym/{gymId}/due")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get due maintenance schedules for gym")
  public ResponseEntity<ApiResponse<List<MaintenanceScheduleResponse>>> getDueMaintenanceSchedules(
      @PathVariable UUID gymId) {
    List<MaintenanceSchedule> schedules = maintenanceService.getDueMaintenanceSchedulesByGym(gymId);
    List<MaintenanceScheduleResponse> responses = schedules.stream()
      .map(MaintenanceScheduleResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PutMapping("/schedules/{id}/complete")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Complete maintenance schedule")
  public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> completeMaintenanceSchedule(
      @PathVariable UUID id,
      @RequestParam UUID maintenanceRecordId) {
    MaintenanceSchedule schedule = maintenanceService.completeMaintenanceSchedule(id, maintenanceRecordId);
    return ResponseEntity.ok(ApiResponse.success(MaintenanceScheduleResponse.fromEntity(schedule), 
      "Maintenance schedule completed successfully"));
  }

  @PutMapping("/schedules/{id}/reschedule")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Reschedule maintenance")
  public ResponseEntity<ApiResponse<MaintenanceScheduleResponse>> rescheduleMaintenance(
      @PathVariable UUID id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
    MaintenanceSchedule schedule = maintenanceService.rescheduleMaintenance(id, newDate);
    return ResponseEntity.ok(ApiResponse.success(MaintenanceScheduleResponse.fromEntity(schedule), 
      "Maintenance rescheduled successfully"));
  }
}
