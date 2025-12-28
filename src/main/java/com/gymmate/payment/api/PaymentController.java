package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.*;
import com.gymmate.payment.application.StripePaymentService;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for gym payment management.
 * Handles payment methods and invoices for platform subscriptions (Gym → GymMate).
 */
@RestController
@RequestMapping("/api/subscriptions/payments")
@RequiredArgsConstructor
@Tag(name = "Subscription Payments", description = "Payment management for gym subscriptions")
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Attach payment method", description = "Attach a new payment method to the gym's subscription")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> attachPaymentMethod(
            @Valid @RequestBody AttachPaymentMethodRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        PaymentMethodResponse response = stripePaymentService.attachPaymentMethod(
                gymId,
                request.getStripePaymentMethodId(),
                request.getSetAsDefault() != null ? request.getSetAsDefault() : true
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Payment method attached successfully"));
    }

    @GetMapping("/methods")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get payment methods", description = "Get all payment methods for the gym")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<PaymentMethodResponse> methods = stripePaymentService.getPaymentMethods(gymId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @DeleteMapping("/methods/{id}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Remove payment method", description = "Remove a payment method from the gym")
    public ResponseEntity<ApiResponse<Void>> removePaymentMethod(@PathVariable UUID id) {
        UUID gymId = TenantContext.getCurrentTenantId();
        stripePaymentService.removePaymentMethod(gymId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed successfully"));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get invoices", description = "Get all invoices for the gym's subscription")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<InvoiceResponse> invoices = stripePaymentService.getInvoices(gymId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }
}

