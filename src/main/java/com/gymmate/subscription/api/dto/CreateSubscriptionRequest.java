package com.gymmate.subscription.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotBlank(message = "Tier name is required")
    private String tierName;

    @Builder.Default
    private Boolean startTrial = false;

    /**
     * Provider payment method code/token for automatic billing.
     */
    private String paymentMethodId;

    /**
     * Whether to create the subscription in the configured provider for automatic billing.
     * If false, subscription is tracked locally only (for manual/invoice billing).
     */
    @Builder.Default
    private Boolean enableProviderBilling = true;
}

