package com.gymmate.notification.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.domain.SnsProcessedMessage;
import com.gymmate.notification.domain.SuppressionReason;
import com.gymmate.notification.infrastructure.SnsProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service processing incoming Amazon SNS webhooks for SES event delivery.
 * Handles subscription handshake confirmation, cryptographic verification,
 * deduplication, and suppression list updates for bounces/complaints.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SesWebhookService {

    private final ObjectMapper objectMapper;
    private final SnsSignatureVerifier signatureVerifier;
    private final SnsProcessedMessageRepository processedMessageRepository;
    private final EmailSuppressionService suppressionService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Process raw SNS JSON payload.
     */
    public void processSnsPayload(String rawPayload) {
        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            String messageType = root.path("Type").asText("");

            if ("SubscriptionConfirmation".equalsIgnoreCase(messageType)) {
                handleSubscriptionConfirmation(root);
                return;
            }

            if ("Notification".equalsIgnoreCase(messageType)) {
                handleNotification(root);
                return;
            }

            if ("UnsubscribeConfirmation".equalsIgnoreCase(messageType)) {
                log.info("Received SNS UnsubscribeConfirmation");
                return;
            }

            log.warn("Unrecognised SNS message type: {}", messageType);
        } catch (Exception e) {
            log.error("Error processing SNS payload: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle the SNS Subscription confirmation handshake by executing a GET on SubscribeURL.
     */
    private void handleSubscriptionConfirmation(JsonNode root) {
        String subscribeUrl = root.path("SubscribeURL").asText(null);
        String topicArn = root.path("TopicArn").asText("");
        log.info("Received SNS SubscriptionConfirmation for topic: {}", topicArn);

        if (subscribeUrl == null || subscribeUrl.isBlank()) {
            log.error("Missing SubscribeURL in SubscriptionConfirmation payload");
            return;
        }

        // Verify signature before executing GET
        if (!signatureVerifier.verifySignature(root)) {
            log.error("SNS signature verification failed for SubscriptionConfirmation");
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(subscribeUrl))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Successfully confirmed SNS subscription for topic {}. Status: {}", topicArn, response.statusCode());
            } else {
                log.error("Failed to confirm SNS subscription for topic {}. HTTP status: {}", topicArn, response.statusCode());
            }
        } catch (Exception e) {
            log.error("Exception occurred while confirming SNS subscription: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle a notification message from SNS containing SES event data.
     */
    @Transactional
    public void handleNotification(JsonNode root) {
        String messageId = root.path("MessageId").asText(null);
        String topicArn = root.path("TopicArn").asText("");

        if (messageId == null || messageId.isBlank()) {
            log.error("SNS Notification missing MessageId");
            return;
        }

        // 1. Check idempotency
        if (processedMessageRepository.existsByMessageId(messageId)) {
            log.debug("Skipping already processed SNS message: {}", messageId);
            return;
        }

        // 2. Verify signature
        if (!signatureVerifier.verifySignature(root)) {
            log.error("Signature verification failed for SNS messageId: {}", messageId);
            return;
        }

        // 3. Parse SES Event from Message content
        String messageContent = root.path("Message").asText("");
        String eventType = "UNKNOWN";

        try {
            JsonNode sesEvent = objectMapper.readTree(messageContent);
            eventType = resolveEventType(sesEvent);

            processSesEvent(eventType, sesEvent);

            // 4. Save processed record for idempotency
            SnsProcessedMessage processedRecord = SnsProcessedMessage.builder()
                    .messageId(messageId)
                    .topicArn(topicArn)
                    .messageType("Notification")
                    .eventType(eventType)
                    .build();
            processedMessageRepository.save(processedRecord);

            log.info("Successfully processed SNS SES event [{}] for messageId: {}", eventType, messageId);
        } catch (Exception e) {
            log.error("Error parsing/processing SES event JSON inside SNS message {}: {}", messageId, e.getMessage(), e);
        }
    }

    private String resolveEventType(JsonNode sesEvent) {
        if (sesEvent.hasNonNull("eventType")) {
            return sesEvent.path("eventType").asText();
        }
        if (sesEvent.hasNonNull("notificationType")) {
            return sesEvent.path("notificationType").asText();
        }
        return "UNKNOWN";
    }

    private void processSesEvent(String eventType, JsonNode sesEvent) {
        switch (eventType.toUpperCase()) {
            case "BOUNCE" -> handleBounce(sesEvent);
            case "COMPLAINT" -> handleComplaint(sesEvent);
            case "DELIVERY" -> handleDelivery(sesEvent);
            case "REJECT" -> handleReject(sesEvent);
            case "RENDERING FAILURE", "RENDERING_FAILURE" -> handleRenderingFailure(sesEvent);
            default -> log.debug("Received SES event type: {}", eventType);
        }
    }

    private void handleBounce(JsonNode sesEvent) {
        JsonNode bounceNode = sesEvent.path("bounce");
        String bounceType = bounceNode.path("bounceType").asText("Permanent");
        String bounceSubType = bounceNode.path("bounceSubType").asText("General");

        JsonNode recipients = bounceNode.path("bouncedRecipients");
        if (recipients.isArray()) {
            for (JsonNode recipient : recipients) {
                String email = recipient.path("emailAddress").asText();
                String diagnostic = recipient.path("diagnosticCode").asText(null);

                if ("Permanent".equalsIgnoreCase(bounceType)) {
                    suppressionService.suppressPermanent(email, SuppressionReason.PERMANENT_BOUNCE, bounceType, bounceSubType, diagnostic, null, null);
                    log.error("SES Hard Bounce: Suppressed {} (subType: {}, diagnostic: {})", email, bounceSubType, diagnostic);
                } else {
                    suppressionService.recordTransientBounce(email, bounceSubType, diagnostic, null, null);
                    log.warn("SES Transient Bounce: Recorded soft bounce for {} (subType: {})", email, bounceSubType);
                }
            }
        }
    }

    private void handleComplaint(JsonNode sesEvent) {
        JsonNode complaintNode = sesEvent.path("complaint");
        String feedbackType = complaintNode.path("complaintFeedbackType").asText("abuse");

        JsonNode recipients = complaintNode.path("complainedRecipients");
        if (recipients.isArray()) {
            for (JsonNode recipient : recipients) {
                String email = recipient.path("emailAddress").asText();
                suppressionService.suppressPermanent(email, SuppressionReason.COMPLAINT, "Complaint", feedbackType, "FeedbackType: " + feedbackType, null, null);
                log.error("SES Spam Complaint: Permanently suppressed {} (feedback: {})", email, feedbackType);
            }
        }
    }

    private void handleDelivery(JsonNode sesEvent) {
        JsonNode mail = sesEvent.path("mail");
        String messageId = mail.path("messageId").asText();
        log.debug("SES Delivery confirmed for messageId: {}", messageId);
    }

    private void handleReject(JsonNode sesEvent) {
        JsonNode mail = sesEvent.path("mail");
        String reason = sesEvent.path("reject").path("reason").asText("Unknown");
        log.error("SES Pre-send Reject for messageId: {}. Reason: {}", mail.path("messageId").asText(), reason);
    }

    private void handleRenderingFailure(JsonNode sesEvent) {
        JsonNode failure = sesEvent.path("failure");
        String errorMessage = failure.path("errorMessage").asText();
        String templateName = failure.path("templateName").asText();
        log.error("SES Rendering Failure for template: {}. Error: {}", templateName, errorMessage);
    }
}
