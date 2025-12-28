package com.gymmate.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to attach a payment method to a gym's subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachPaymentMethodRequest {

    @NotBlank(message = "Stripe payment method ID is required")
    private String stripePaymentMethodId;

    private Boolean setAsDefault = true;
}

