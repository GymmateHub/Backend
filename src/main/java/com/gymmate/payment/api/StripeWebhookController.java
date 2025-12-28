package com.gymmate.payment.api;

import com.gymmate.payment.application.StripeWebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling Stripe webhook events.
 * These endpoints receive webhook events from Stripe and process them accordingly.
 */
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stripe Webhooks", description = "Webhook endpoints for Stripe events")
public class StripeWebhookController {

    private final StripeWebhookService webhookService;

    /**
     * Handle platform webhook events (for gym subscriptions to GymMate).
     * Events like invoice.paid, customer.subscription.updated, etc.
     */
    @PostMapping("/platform")
    @Operation(summary = "Platform webhook", description = "Handle Stripe platform webhook events")
    public ResponseEntity<String> handlePlatformWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.debug("Received platform webhook");
        webhookService.processPlatformWebhook(payload, signature);
        return ResponseEntity.ok("Processed");
    }

    /**
     * Handle Connect webhook events (for member payments to gyms).
     * Events like account.updated, payment_intent.succeeded, etc.
     */
    @PostMapping("/connect")
    @Operation(summary = "Connect webhook", description = "Handle Stripe Connect webhook events")
    public ResponseEntity<String> handleConnectWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.debug("Received Connect webhook");
        webhookService.processConnectWebhook(payload, signature);
        return ResponseEntity.ok("Processed");
    }
}

