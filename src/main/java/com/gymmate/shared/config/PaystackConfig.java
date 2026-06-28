package com.gymmate.shared.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PaystackConfig {

    @Value("${paystack.secret-key:}")
    private String secretKey;

    @Value("${paystack.public-key:}")
    private String publicKey;

    @Value("${paystack.base-url:https://api.paystack.co}")
    private String baseUrl;

    @Value("${paystack.webhook-secret:}")
    private String webhookSecret;

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }
}

