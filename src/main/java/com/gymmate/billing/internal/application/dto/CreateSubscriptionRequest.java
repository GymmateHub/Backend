package com.gymmate.billing.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSubscriptionRequest(
        @NotBlank(message = "Tier name is required")
        String tierName,
        Boolean startTrial,
        // Stripe PaymentMethod ID (pm_xxx) collected from Stripe Elements on the frontend.
        // Required when startTrial is true to enable automatic billing after trial ends.
        String paymentMethodId,
        // Whether to create the subscription in Stripe for automatic billing.
        // If false, subscription is tracked locally only (for manual/invoice billing).
        Boolean enableStripeBilling
) {
    public CreateSubscriptionRequest {
        if (startTrial == null) startTrial = false;
        if (enableStripeBilling == null) enableStripeBilling = true;
    }
}
