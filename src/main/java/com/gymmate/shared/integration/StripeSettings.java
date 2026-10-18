package com.gymmate.shared.integration;

/**
 * Stripe account settings as seen by application services (implemented by the Stripe
 * configuration in infrastructure).
 */
public interface StripeSettings {

    /** Whether a usable Stripe API key is configured. */
    boolean isConfigured();

    String getWebhookSecret();

    String getConnectWebhookSecret();

    /** Platform fee charged on gym-to-member payments, in percent. */
    Double getApplicationFeePercent();
}
