package com.gymmate.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response containing payment method details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponse {

    private UUID id;
    private String type;
    private String cardBrand;
    private String lastFour;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean isDefault;
}

