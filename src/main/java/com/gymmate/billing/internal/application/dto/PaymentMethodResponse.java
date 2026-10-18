package com.gymmate.billing.internal.application.dto;

import java.util.UUID;

/**
 * Response containing payment method details.
 */
public record PaymentMethodResponse(
        UUID id,
        String type,
        String cardBrand,
        String lastFour,
        Integer expiryMonth,
        Integer expiryYear,
        Boolean isDefault
) {
}
