package com.gymmate.billing.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to attach a payment method to a gym's subscription.
 */
public record AttachPaymentMethodRequest(
        @NotBlank(message = "Stripe payment method ID is required")
        String stripePaymentMethodId,
        Boolean setAsDefault
) {
    public AttachPaymentMethodRequest {
        if (setAsDefault == null) setAsDefault = true;
    }
}
