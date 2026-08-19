package com.gymmate.notification.internal.web;

import com.gymmate.notification.application.EmailSuppressionService;
import com.gymmate.notification.application.SesWebhookService;
import com.gymmate.notification.domain.SuppressionReason;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public webhook endpoint for Amazon SNS / SES event delivery and one-click unsubscribes.
 */
@RestController
@RequestMapping("/api/webhooks/ses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SES Webhooks", description = "Webhook endpoints for Amazon SES & SNS deliverability events")
public class SesWebhookController {

    private final SesWebhookService sesWebhookService;
    private final EmailSuppressionService suppressionService;

    /**
     * Receive Amazon SNS notifications containing SES event feedback (bounces, complaints, delivery, rejects).
     * Also handles initial subscription confirmation handshake.
     */
    @PostMapping("/events")
    @Operation(summary = "SES Event Webhook", description = "Processes SNS topic notifications for SES deliverability")
    public ResponseEntity<String> handleSesEvent(@RequestBody String payload) {
        log.debug("Received SNS SES event payload");
        sesWebhookService.processSnsPayload(payload);
        return ResponseEntity.ok("OK");
    }

    /**
     * RFC 8058 One-Click Unsubscribe endpoint.
     */
    @PostMapping("/unsubscribe")
    @Operation(summary = "One-Click Unsubscribe", description = "Handles RFC 8058 compliant one-click unsubscribe requests")
    public ResponseEntity<String> handleOneClickUnsubscribe(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String token) {

        if (email != null && !email.isBlank()) {
            suppressionService.suppressPermanent(
                    email,
                    SuppressionReason.UNSUBSCRIBED,
                    "Unsubscribed",
                    "OneClick",
                    "RFC 8058 One-Click Unsubscribe",
                    null,
                    null);
            log.info("Processed one-click unsubscribe for {}", email);
        }

        return ResponseEntity.ok("Unsubscribed successfully");
    }

    /**
     * GET endpoint for web-based unsubscribe clicks.
     */
    @GetMapping("/unsubscribe")
    @Operation(summary = "Web Unsubscribe", description = "Handles browser-based unsubscribe requests")
    public ResponseEntity<String> handleWebUnsubscribe(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            suppressionService.suppressPermanent(
                    email,
                    SuppressionReason.UNSUBSCRIBED,
                    "Unsubscribed",
                    "WebClick",
                    "Web Unsubscribe",
                    null,
                    null);
        }
        return ResponseEntity.ok("You have been unsubscribed from our email communications.");
    }
}
