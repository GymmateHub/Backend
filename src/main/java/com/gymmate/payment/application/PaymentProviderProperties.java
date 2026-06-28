package com.gymmate.payment.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Runtime provider switch for billing operations.
 */
@Component
@ConfigurationProperties(prefix = "payments")
@Getter
@Setter
public class PaymentProviderProperties {

    private Provider provider = Provider.STRIPE;

    public enum Provider {
        STRIPE,
        PAYSTACK
    }
}

