package com.gymmate.payment.api.dto;

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

    @jakarta.validation.constraints.NotBlank(message = "Payment method ID is required")
    private String providerPaymentMethodId;

    private Boolean setAsDefault = true;
}

