package com.gymmate.inventory.api;

import com.gymmate.inventory.api.dto.*;
import com.gymmate.inventory.application.EquipmentService;
import com.gymmate.inventory.domain.Equipment;
import com.gymmate.inventory.domain.EquipmentStatus;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for equipment management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Equipment Management APIs")
public class EquipmentController {

  private final EquipmentService equipmentService;

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create equipment", description = "Create new equipment")
  public ResponseEntity<ApiResponse<EquipmentResponse>> createEquipment(
      @Valid @RequestBody EquipmentCreateRequest request) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    
    Equipment equipment = Equipment.builder()
      .name(request.name())
      .category(request.category())
      .description(request.description())
      .manufacturer(request.manufacturer())
      .model(request.model())
      .serialNumber(request.serialNumber())
      .purchaseDate(request.purchaseDate())
      .purchasePrice(request.purchasePrice())
      .currentValue(request.currentValue())
      .warrantyExpiryDate(request.warrantyExpiryDate())
      .warrantyProvider(request.warrantyProvider())
      .areaId(request.areaId())
      .locationNotes(request.locationNotes())
      .maintenanceIntervalDays(request.maintenanceIntervalDays())
      .maxCapacity(request.maxCapacity())
      .supplierId(request.supplierId())
      .imageUrl(request.imageUrl())
      .notes(request.notes())
      .build();
    
    equipment.setOrganisationId(organisationId);
    equipment.setGymId(request.gymId());
    
    Equipment created = equipmentService.createEquipment(equipment);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(EquipmentResponse.fromEntity(created), "Equipment created successfully"));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Update equipment", description = "Update equipment details")
  public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipment(
      @PathVariable UUID id,
      @Valid @RequestBody EquipmentUpdateRequest request) {
    
    Equipment equipment = Equipment.builder()
      .name(request.name())
      .category(request.category())
      .description(request.description())
      .manufacturer(request.manufacturer())
      .model(request.model())
      .serialNumber(request.serialNumber())
      .areaId(request.areaId())
      .locationNotes(request.locationNotes())
      .imageUrl(request.imageUrl())
      .notes(request.notes())
      .build();
    
    Equipment updated = equipmentService.updateEquipment(id, equipment);
    return ResponseEntity.ok(ApiResponse.success(EquipmentResponse.fromEntity(updated), 
      "Equipment updated successfully"));
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Update equipment status", description = "Update equipment status")
  public ResponseEntity<ApiResponse<EquipmentResponse>> updateEquipmentStatus(
      @PathVariable UUID id,
      @RequestParam EquipmentStatus status) {
    
    Equipment updated = equipmentService.updateEquipmentStatus(id, status);
    return ResponseEntity.ok(ApiResponse.success(EquipmentResponse.fromEntity(updated), 
      "Equipment status updated successfully"));
  }

  @PostMapping("/{id}/maintenance")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Record maintenance", description = "Record maintenance on equipment")
  public ResponseEntity<ApiResponse<EquipmentResponse>> recordMaintenance(
      @PathVariable UUID id,
      @Valid @RequestBody MaintenanceRecordRequest request) {
    
    Equipment updated = equipmentService.recordMaintenance(id, request.maintenanceDate(), request.cost());
    return ResponseEntity.ok(ApiResponse.success(EquipmentResponse.fromEntity(updated), 
      "Maintenance recorded successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Delete equipment", description = "Delete equipment")
  public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable UUID id) {
    equipmentService.deleteEquipment(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Equipment deleted successfully"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  @Operation(summary = "Get equipment by ID", description = "Get equipment details by ID")
  public ResponseEntity<ApiResponse<EquipmentResponse>> getEquipmentById(@PathVariable UUID id) {
    Equipment equipment = equipmentService.getEquipmentById(id);
    return ResponseEntity.ok(ApiResponse.success(EquipmentResponse.fromEntity(equipment)));
  }

  @GetMapping("/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  @Operation(summary = "Get equipment by gym", description = "Get all equipment for a gym")
  public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getEquipmentByGym(@PathVariable UUID gymId) {
    List<Equipment> equipment = equipmentService.getEquipmentByGym(gymId);
    List<EquipmentResponse> responses = equipment.stream()
      .map(EquipmentResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/organisation")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Get equipment by organisation", description = "Get all equipment for current organisation")
  public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getEquipmentByOrganisation() {
    UUID organisationId = TenantContext.getCurrentTenantId();
    List<Equipment> equipment = equipmentService.getEquipmentByOrganisation(organisationId);
    List<EquipmentResponse> responses = equipment.stream()
      .map(EquipmentResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/active")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  @Operation(summary = "Get active equipment by gym", description = "Get active equipment for a gym")
  public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getActiveEquipmentByGym(@PathVariable UUID gymId) {
    List<Equipment> equipment = equipmentService.getActiveEquipmentByGym(gymId);
    List<EquipmentResponse> responses = equipment.stream()
      .map(EquipmentResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/maintenance-due")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get maintenance due equipment", description = "Get equipment due for maintenance")
  public ResponseEntity<ApiResponse<List<EquipmentResponse>>> getMaintenanceDueByGym(@PathVariable UUID gymId) {
    List<Equipment> equipment = equipmentService.getMaintenanceDueByGym(gymId);
    List<EquipmentResponse> responses = equipment.stream()
      .map(EquipmentResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }
}
