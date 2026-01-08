package com.gymmate.inventory.api;

import com.gymmate.inventory.api.dto.*;
import com.gymmate.inventory.application.InventoryService;
import com.gymmate.inventory.domain.InventoryItem;
import com.gymmate.inventory.domain.StockMovement;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for inventory management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/items")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory Management APIs")
public class InventoryController {

  private final InventoryService inventoryService;

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create inventory item")
  public ResponseEntity<ApiResponse<InventoryItemResponse>> createInventoryItem(
      @Valid @RequestBody InventoryItemCreateRequest request) {
    
    UUID organisationId = TenantContext.getCurrentTenantId();
    
    InventoryItem item = InventoryItem.builder()
      .name(request.name())
      .sku(request.sku())
      .category(request.category())
      .description(request.description())
      .minimumStock(request.minimumStock())
      .maximumStock(request.maximumStock())
      .reorderPoint(request.reorderPoint())
      .reorderQuantity(request.reorderQuantity())
      .unitCost(request.unitCost())
      .unitPrice(request.unitPrice())
      .unit(request.unit())
      .supplierId(request.supplierId())
      .supplierProductCode(request.supplierProductCode())
      .barcode(request.barcode())
      .location(request.location())
      .expiryTracking(request.expiryTracking())
      .batchTracking(request.batchTracking())
      .imageUrl(request.imageUrl())
      .notes(request.notes())
      .build();
    
    item.setOrganisationId(organisationId);
    item.setGymId(request.gymId());
    
    InventoryItem created = inventoryService.createInventoryItem(item);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(InventoryItemResponse.fromEntity(created), 
        "Inventory item created successfully"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get inventory item by ID")
  public ResponseEntity<ApiResponse<InventoryItemResponse>> getInventoryItem(@PathVariable UUID id) {
    InventoryItem item = inventoryService.getInventoryItemById(id);
    return ResponseEntity.ok(ApiResponse.success(InventoryItemResponse.fromEntity(item)));
  }

  @GetMapping("/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get inventory items by gym")
  public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryItemsByGym(@PathVariable UUID gymId) {
    List<InventoryItem> items = inventoryService.getInventoryItemsByGym(gymId);
    List<InventoryItemResponse> responses = items.stream()
      .map(InventoryItemResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/low-stock")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get low stock items")
  public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getLowStockItems(@PathVariable UUID gymId) {
    List<InventoryItem> items = inventoryService.getLowStockItemsByGym(gymId);
    List<InventoryItemResponse> responses = items.stream()
      .map(InventoryItemResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/gym/{gymId}/reorder-needed")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get items needing reorder")
  public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getReorderNeeded(@PathVariable UUID gymId) {
    List<InventoryItem> items = inventoryService.getReorderNeededByGym(gymId);
    List<InventoryItemResponse> responses = items.stream()
      .map(InventoryItemResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PostMapping("/{id}/purchase")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Record stock purchase")
  public ResponseEntity<ApiResponse<StockMovementResponse>> recordPurchase(
      @PathVariable UUID id,
      @Valid @RequestBody StockMovementRequest request) {
    
    StockMovement movement = inventoryService.recordPurchase(
      id, 
      request.quantity(), 
      request.unitCost(), 
      request.supplierId(),
      request.referenceNumber(), 
      request.notes()
    );
    return ResponseEntity.ok(ApiResponse.success(StockMovementResponse.fromEntity(movement), 
      "Purchase recorded successfully"));
  }

  @PostMapping("/{id}/sale")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Record stock sale")
  public ResponseEntity<ApiResponse<StockMovementResponse>> recordSale(
      @PathVariable UUID id,
      @Valid @RequestBody StockMovementRequest request) {
    
    StockMovement movement = inventoryService.recordSale(
      id, 
      request.quantity(), 
      request.unitCost(), 
      request.customerId(),
      request.referenceNumber(), 
      request.notes()
    );
    return ResponseEntity.ok(ApiResponse.success(StockMovementResponse.fromEntity(movement), 
      "Sale recorded successfully"));
  }

  @PostMapping("/{id}/adjustment")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Record stock adjustment")
  public ResponseEntity<ApiResponse<StockMovementResponse>> recordAdjustment(
      @PathVariable UUID id,
      @RequestParam int newStock,
      @RequestParam(required = false) String reason,
      @RequestParam(required = false) String performedBy) {
    
    StockMovement movement = inventoryService.recordAdjustment(id, newStock, reason, performedBy);
    return ResponseEntity.ok(ApiResponse.success(StockMovementResponse.fromEntity(movement), 
      "Adjustment recorded successfully"));
  }

  @PostMapping("/{id}/damage")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Record damaged stock")
  public ResponseEntity<ApiResponse<StockMovementResponse>> recordDamage(
      @PathVariable UUID id,
      @RequestParam int quantity,
      @RequestParam(required = false) String reason,
      @RequestParam(required = false) String performedBy) {
    
    StockMovement movement = inventoryService.recordDamage(id, quantity, reason, performedBy);
    return ResponseEntity.ok(ApiResponse.success(StockMovementResponse.fromEntity(movement), 
      "Damage recorded successfully"));
  }

  @GetMapping("/{id}/movements")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get stock movements for item")
  public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovements(@PathVariable UUID id) {
    List<StockMovement> movements = inventoryService.getStockMovementsByItem(id);
    List<StockMovementResponse> responses = movements.stream()
      .map(StockMovementResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/movements/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get stock movements for gym")
  public ResponseEntity<ApiResponse<List<StockMovementResponse>>> getStockMovementsByGym(
      @PathVariable UUID gymId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
    
    List<StockMovement> movements;
    if (startDate != null && endDate != null) {
      movements = inventoryService.getStockMovementsByGymAndDateRange(gymId, startDate, endDate);
    } else {
      movements = inventoryService.getStockMovementsByGym(gymId);
    }
    
    List<StockMovementResponse> responses = movements.stream()
      .map(StockMovementResponse::fromEntity)
      .collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }
}
