package com.gymmate.pos.api;

import com.gymmate.pos.api.dto.*;
import com.gymmate.pos.application.PosService;
import com.gymmate.pos.domain.Sale;
import com.gymmate.pos.domain.SaleStatus;
import com.gymmate.pos.domain.CashDrawer;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for Point of Sale (POS) operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@Tag(name = "Point of Sale", description = "POS Sales and Cash Drawer Management APIs")
public class PosController {

    private final PosService posService;

    // ===== SALE ENDPOINTS =====

    @PostMapping("/sales")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create a new sale")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID staffId = getStaffIdFromUser(userDetails);
        Sale sale = posService.createSale(request, staffId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SaleResponse.fromEntity(sale), "Sale created successfully"));
    }

    @PostMapping("/sales/quick")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create and complete a sale in one step")
    public ResponseEntity<ApiResponse<SaleResponse>> createAndCompleteSale(
            @Valid @RequestBody CreateSaleRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID staffId = getStaffIdFromUser(userDetails);
        BigDecimal amountPaid = request.amountPaid() != null ? request.amountPaid() : BigDecimal.ZERO;
        Sale sale = posService.createAndCompleteSale(request, staffId, amountPaid);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SaleResponse.fromEntity(sale), "Sale completed successfully"));
    }

    @PostMapping("/sales/{saleId}/complete")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Complete a pending sale with payment")
    public ResponseEntity<ApiResponse<SaleResponse>> completeSale(
            @PathVariable UUID saleId,
            @Valid @RequestBody CompleteSaleRequest request) {

        Sale sale = posService.completeSale(
                saleId,
                request.paymentType(),
                request.amountPaid(),
                request.stripePaymentIntentId());

        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale), "Sale completed successfully"));
    }

    @PostMapping("/sales/{saleId}/cancel")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Cancel a pending sale")
    public ResponseEntity<ApiResponse<SaleResponse>> cancelSale(@PathVariable UUID saleId) {
        Sale sale = posService.cancelSale(saleId);
        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale), "Sale cancelled successfully"));
    }

    @PostMapping("/sales/{saleId}/refund")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Refund a completed sale (full or partial)")
    public ResponseEntity<ApiResponse<SaleResponse>> refundSale(
            @PathVariable UUID saleId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String reason) {

        Sale sale = posService.refundSale(saleId, amount, reason);
        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale), "Refund processed successfully"));
    }

    @PostMapping("/sales/{saleId}/items")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Add item to a pending sale")
    public ResponseEntity<ApiResponse<SaleResponse>> addItemToSale(
            @PathVariable UUID saleId,
            @Valid @RequestBody SaleItemRequest request) {

        Sale sale = posService.addItemToSale(saleId, request);
        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale), "Item added successfully"));
    }

    @DeleteMapping("/sales/{saleId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Remove item from a pending sale")
    public ResponseEntity<ApiResponse<SaleResponse>> removeItemFromSale(
            @PathVariable UUID saleId,
            @PathVariable UUID itemId) {

        Sale sale = posService.removeItemFromSale(saleId, itemId);
        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale), "Item removed successfully"));
    }

    @GetMapping("/sales/{saleId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(@PathVariable UUID saleId) {
        Sale sale = posService.getSaleById(saleId);
        return ResponseEntity.ok(ApiResponse.success(SaleResponse.fromEntity(sale)));
    }

    @GetMapping("/sales/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get all sales for a gym")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByGym(
            @PathVariable UUID gymId,
            @RequestParam(required = false) SaleStatus status) {

        List<Sale> sales = status != null
                ? posService.getSalesByGymAndStatus(gymId, status)
                : posService.getSalesByGym(gymId);

        List<SaleResponse> responses = sales.stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/sales/gym/{gymId}/today")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get today's sales for a gym")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getTodaysSales(@PathVariable UUID gymId) {
        List<Sale> sales = posService.getTodaysSales(gymId);
        List<SaleResponse> responses = sales.stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/sales/gym/{gymId}/daterange")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get sales for a gym within date range")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByDateRange(
            @PathVariable UUID gymId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Sale> sales = posService.getSalesByDateRange(gymId, startDate, endDate);
        List<SaleResponse> responses = sales.stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/sales/member/{memberId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    @Operation(summary = "Get sales for a member")
    public ResponseEntity<ApiResponse<List<SaleResponse>>> getSalesByMember(@PathVariable UUID memberId) {
        List<Sale> sales = posService.getSalesByMember(memberId);
        List<SaleResponse> responses = sales.stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ===== CASH DRAWER ENDPOINTS =====

    @PostMapping("/drawer/open")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Open a new cash drawer session")
    public ResponseEntity<ApiResponse<CashDrawerResponse>> openCashDrawer(
            @Valid @RequestBody OpenCashDrawerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID staffId = getStaffIdFromUser(userDetails);
        CashDrawer drawer = posService.openCashDrawer(
                request.gymId(),
                staffId,
                request.openingBalance(),
                request.notes());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CashDrawerResponse.fromEntity(drawer), "Cash drawer opened successfully"));
    }

    @PostMapping("/drawer/{drawerId}/close")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Close an open cash drawer session")
    public ResponseEntity<ApiResponse<CashDrawerResponse>> closeCashDrawer(
            @PathVariable UUID drawerId,
            @Valid @RequestBody CloseCashDrawerRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID staffId = getStaffIdFromUser(userDetails);
        CashDrawer drawer = posService.closeCashDrawer(
                drawerId,
                staffId,
                request.closingBalance(),
                request.closingNotes());

        return ResponseEntity
                .ok(ApiResponse.success(CashDrawerResponse.fromEntity(drawer), "Cash drawer closed successfully"));
    }

    @GetMapping("/drawer/gym/{gymId}/current")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get current open cash drawer for a gym")
    public ResponseEntity<ApiResponse<CashDrawerResponse>> getCurrentCashDrawer(@PathVariable UUID gymId) {
        return posService.getOpenCashDrawer(gymId)
                .map(drawer -> ResponseEntity.ok(ApiResponse.success(CashDrawerResponse.fromEntity(drawer))))
                .orElse(ResponseEntity.ok(ApiResponse.success(null, "No open cash drawer")));
    }

    @GetMapping("/drawer/gym/{gymId}/history")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get cash drawer history for a gym")
    public ResponseEntity<ApiResponse<List<CashDrawerResponse>>> getCashDrawerHistory(
            @PathVariable UUID gymId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<CashDrawer> drawers = posService.getCashDrawerHistory(gymId, startDate, endDate);
        List<CashDrawerResponse> responses = drawers.stream()
                .map(CashDrawerResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // ===== REPORTING ENDPOINTS =====

    @GetMapping("/reports/gym/{gymId}/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get sales summary for a gym")
    public ResponseEntity<ApiResponse<PosService.PosSalesSummary>> getSalesSummary(
            @PathVariable UUID gymId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        PosService.PosSalesSummary summary = posService.getSalesSummary(gymId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/reports/gym/{gymId}/top-items")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get top selling items for a gym")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTopSellingItems(
            @PathVariable UUID gymId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Object[]> topItems = posService.getTopSellingItems(gymId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(topItems));
    }

    // ===== HELPER METHODS =====

    private UUID getStaffIdFromUser(UserDetails userDetails) {
        // Try to parse the username as UUID first
        try {
            return UUID.fromString(userDetails.getUsername());
        } catch (IllegalArgumentException e) {
            // If not a UUID, use a hash-based approach as fallback
            return UUID.nameUUIDFromBytes(userDetails.getUsername().getBytes());
        }
    }
}
