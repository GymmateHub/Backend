package com.gymmate.membership.api;

import com.gymmate.membership.application.MemberPaymentService;
import com.gymmate.membership.application.MemberPaymentService.MemberInvoiceResponse;
import com.gymmate.membership.application.MemberPaymentService.MemberPaymentMethodResponse;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for member payment operations.
 * Handles member payment methods and invoices for gym memberships.
 */
@RestController
@RequestMapping("/api/memberships/payments")
@RequiredArgsConstructor
@Tag(name = "Member Payments", description = "Payment management for member memberships")
public class MemberPaymentController {

    private final MemberPaymentService memberPaymentService;

    @PostMapping("/methods")
    @PreAuthorize("hasAnyRole('MEMBER', 'GYM_OWNER', 'MANAGER', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Attach payment method", description = "Attach a payment method for a member")
    public ResponseEntity<ApiResponse<MemberPaymentMethodResponse>> attachPaymentMethod(
            @Valid @RequestBody AttachMemberPaymentMethodRequest request) {

        UUID gymId = TenantContext.getCurrentTenantId();
        MemberPaymentMethodResponse response = memberPaymentService.attachPaymentMethod(
                gymId,
                request.getMemberId(),
                request.getEmail(),
                request.getName(),
                request.getStripePaymentMethodId(),
                request.getSetAsDefault() != null ? request.getSetAsDefault() : true
        );

        return ResponseEntity.ok(ApiResponse.success(response, "Payment method attached successfully"));
    }

    @GetMapping("/methods/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'GYM_OWNER', 'MANAGER', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Get payment methods", description = "Get all payment methods for a member")
    public ResponseEntity<ApiResponse<List<MemberPaymentMethodResponse>>> getPaymentMethods(
            @PathVariable UUID memberId) {

        UUID gymId = TenantContext.getCurrentTenantId();
        List<MemberPaymentMethodResponse> methods = memberPaymentService.getMemberPaymentMethods(gymId, memberId);
        return ResponseEntity.ok(ApiResponse.success(methods));
    }

    @GetMapping("/invoices/{memberId}")
    @PreAuthorize("hasAnyRole('MEMBER', 'GYM_OWNER', 'MANAGER', 'STAFF', 'SUPER_ADMIN')")
    @Operation(summary = "Get member invoices", description = "Get all invoices for a member's memberships")
    public ResponseEntity<ApiResponse<List<MemberInvoiceResponse>>> getMemberInvoices(
            @PathVariable UUID memberId) {

        UUID gymId = TenantContext.getCurrentTenantId();
        List<MemberInvoiceResponse> invoices = memberPaymentService.getMemberInvoices(gymId, memberId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    @PostMapping("/{membershipId}/cancel")
    @PreAuthorize("hasAnyRole('MEMBER', 'GYM_OWNER', 'MANAGER', 'SUPER_ADMIN')")
    @Operation(summary = "Cancel membership subscription", description = "Cancel a member's subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @PathVariable UUID membershipId,
            @RequestParam(defaultValue = "false") boolean immediate) {

        memberPaymentService.cancelMemberSubscription(membershipId, immediate);
        String message = immediate
                ? "Membership cancelled immediately"
                : "Membership will be cancelled at the end of the billing period";
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }

    // DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachMemberPaymentMethodRequest {
        private UUID memberId;
        @NotBlank(message = "Email is required")
        private String email;
        @NotBlank(message = "Name is required")
        private String name;
        @NotBlank(message = "Stripe payment method ID is required")
        private String stripePaymentMethodId;
        private Boolean setAsDefault = true;
    }
}

