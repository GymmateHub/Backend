package com.gymmate.shared.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe configuration for payment processing.
 * Initializes the Stripe SDK with API keys from environment variables.
 */
@Configuration
@Getter
public class StripeConfig {

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${stripe.connect-webhook-secret:}")
    private String connectWebhookSecret;

    @Value("${stripe.application-fee-percent:1.0}")
    private Double applicationFeePercent;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            Stripe.apiKey = apiKey;
        }
    }

    /**
     * Check if Stripe is properly configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("sk_test_your_test_key_here");
    }
}

