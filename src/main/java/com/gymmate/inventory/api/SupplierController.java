package com.gymmate.inventory.api;

import com.gymmate.inventory.api.dto.SupplierCreateRequest;
import com.gymmate.inventory.api.dto.SupplierResponse;
import com.gymmate.inventory.application.SupplierService;
import com.gymmate.inventory.domain.Supplier;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for supplier management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier Management APIs")
public class SupplierController {

  private final SupplierService supplierService;

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create supplier")
  public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(
      @Valid @RequestBody SupplierCreateRequest request) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    
    Supplier supplier = Supplier.builder()
      .name(request.name())
      .code(request.code())
      .description(request.description())
      .contactPerson(request.contactPerson())
      .email(request.email())
      .phone(request.phone())
      .mobilePhone(request.mobilePhone())
      .website(request.website())
      .address(request.address())
      .city(request.city())
      .state(request.state())
      .country(request.country())
      .postalCode(request.postalCode())
      .taxId(request.taxId())
      .paymentTerms(request.paymentTerms())
      .currency(request.currency())
      .creditLimit(request.creditLimit())
      .supplierCategory(request.supplierCategory())
      .notes(request.notes())
      .build();
    
    supplier.setOrganisationId(organisationId);
    
    Supplier created = supplierService.createSupplier(supplier);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(SupplierResponse.fromEntity(created), 
        "Supplier created successfully"));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Update supplier")
  public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(
      @PathVariable UUID id,
      @Valid @RequestBody SupplierCreateRequest request) {
    
    Supplier supplier = Supplier.builder()
      .name(request.name())
      .code(request.code())
      .description(request.description())
      .contactPerson(request.contactPerson())
      .email(request.email())
      .phone(request.phone())
      .mobilePhone(request.mobilePhone())
      .website(request.website())
      .address(request.address())
      .city(request.city())
      .state(request.state())
      .country(request.country())
      .postalCode(request.postalCode())
      .taxId(request.taxId())
      .paymentTerms(request.paymentTerms())
      .currency(request.currency())
      .creditLimit(request.creditLimit())
      .supplierCategory(request.supplierCategory())
      .notes(request.notes())
      .build();
    
    Supplier updated = supplierService.updateSupplier(id, supplier);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier updated successfully"));
  }

  @PutMapping("/{id}/rating")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Set supplier rating")
  public ResponseEntity<ApiResponse<SupplierResponse>> setSupplierRating(
      @PathVariable UUID id,
      @RequestParam int rating) {
    Supplier updated = supplierService.setSupplierRating(id, rating);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier rating updated successfully"));
  }

  @PutMapping("/{id}/preferred")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Mark supplier as preferred")
  public ResponseEntity<ApiResponse<SupplierResponse>> markAsPreferred(@PathVariable UUID id) {
    Supplier updated = supplierService.markAsPreferred(id);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier marked as preferred"));
  }

  @DeleteMapping("/{id}/preferred")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Unmark supplier as preferred")
  public ResponseEntity<ApiResponse<SupplierResponse>> unmarkAsPreferred(@PathVariable UUID id) {
    Supplier updated = supplierService.unmarkAsPreferred(id);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier unmarked as preferred"));
  }

  @PutMapping("/{id}/deactivate")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Deactivate supplier")
  public ResponseEntity<ApiResponse<SupplierResponse>> deactivateSupplier(@PathVariable UUID id) {
    Supplier updated = supplierService.deactivateSupplier(id);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier deactivated successfully"));
  }

  @PutMapping("/{id}/activate")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Activate supplier")
  public ResponseEntity<ApiResponse<SupplierResponse>> activateSupplier(@PathVariable UUID id) {
    Supplier updated = supplierService.activateSupplier(id);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(updated), 
      "Supplier activated successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Delete supplier")
  public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable UUID id) {
    supplierService.deleteSupplier(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Supplier deleted successfully"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get supplier by ID")
  public ResponseEntity<ApiResponse<SupplierResponse>> getSupplier(@PathVariable UUID id) {
    Supplier supplier = supplierService.getSupplierById(id);
    return ResponseEntity.ok(ApiResponse.success(SupplierResponse.fromEntity(supplier)));
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get all suppliers for organisation")
  public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers(
      @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
      @RequestParam(required = false, defaultValue = "false") boolean preferredOnly) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    List<Supplier> suppliers;
    
    if (preferredOnly) {
      suppliers = supplierService.getPreferredSuppliersByOrganisation(organisationId);
    } else if (activeOnly) {
      suppliers = supplierService.getActiveSuppliersByOrganisation(organisationId);
    } else {
      suppliers = supplierService.getSuppliersByOrganisation(organisationId);
    }
    
    List<SupplierResponse> responses = suppliers.stream()
      .map(SupplierResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }
}
