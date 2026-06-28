package com.gymmate.payment.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.notification.events.PaymentFailedEvent;
import com.gymmate.notification.events.PaymentSuccessEvent;
import com.gymmate.payment.domain.PaymentRefund;
import com.gymmate.payment.domain.PaymentWebhookEvent;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.payment.infrastructure.PaymentWebhookEventRepository;
import com.gymmate.shared.config.PaystackConfig;
import com.gymmate.shared.constants.RefundStatus;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles Paystack webhooks for subscription and member payment lifecycle events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackWebhookService {

    private final PaystackConfig paystackConfig;
    private final ObjectMapper objectMapper;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final MemberInvoiceRepository memberInvoiceRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processWebhook(String payload, String signature) {
        verifySignature(payload, signature);

        JsonNode root = parsePayload(payload);
        String eventType = root.path("event").asText();
        JsonNode data = root.path("data");

        String providerEventId = resolveProviderEventId(eventType, data, payload);
        if (webhookEventRepository.existsByProviderAndProviderEventId("paystack", providerEventId)) {
            log.info("Paystack webhook {} already processed", providerEventId);
            return;
        }

        PaymentWebhookEvent webhookEvent = PaymentWebhookEvent.builder()
                .provider("paystack")
                .providerEventId(providerEventId)
                .eventType(eventType)
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);

        try {
            switch (eventType) {
                case "charge.success" -> handleChargeSuccess(data);
                case "charge.failed" -> handleChargeFailed(data);
                case "refund.processed", "refund.failed" -> handleRefundUpdated(data, eventType);
                default -> log.debug("Unhandled Paystack event type: {}", eventType);
            }
            webhookEvent.markProcessed();
        } catch (Exception ex) {
            webhookEvent.markFailed(ex.getMessage());
            log.error("Failed to process Paystack webhook {}: {}", providerEventId, ex.getMessage());
        }

        webhookEventRepository.save(webhookEvent);
    }

    private void handleChargeSuccess(JsonNode data) {
        String membershipId = metadataValue(data, "membership_id");
        String gymIdValue = metadataValue(data, "gym_id");
        String organisationIdValue = metadataValue(data, "organisation_id");

        BigDecimal amount = fromMinor(data.path("amount").asLong(0));
        String currency = data.path("currency").asText("NGN").toUpperCase();
        String reference = data.path("reference").asText();

        if (membershipId != null) {
            memberMembershipRepository.findById(UUID.fromString(membershipId)).ifPresent(membership -> {
                if (membership.getStatus() == MembershipStatus.EXPIRED
                        || membership.getStatus() == MembershipStatus.CANCELLED
                        || membership.getStatus() == MembershipStatus.PAST_DUE) {
                    membership.setStatus(MembershipStatus.ACTIVE);
                    memberMembershipRepository.save(membership);
                }

                MemberInvoice invoice = MemberInvoice.builder()
                        .memberId(membership.getMemberId())
                        .membershipId(membership.getId())
                        .amount(amount)
                        .currency(currency)
                        .status(MemberInvoiceStatus.PAID)
                        .description("Membership payment via Paystack")
                        .paidAt(LocalDateTime.now())
                        .build();
                invoice.setGymId(membership.getGymId());
                invoice.setOrganisationId(membership.getOrganisationId());
                memberInvoiceRepository.save(invoice);
            });
        }

        if (gymIdValue != null && organisationIdValue != null) {
            eventPublisher.publishEvent(PaymentSuccessEvent.builder()
                    .organisationId(UUID.fromString(organisationIdValue))
                    .gymId(UUID.fromString(gymIdValue))
                    .amount(amount)
                    .invoiceNumber(reference)
                    .build());
        }
    }

    private void handleChargeFailed(JsonNode data) {
        String membershipId = metadataValue(data, "membership_id");
        String gymIdValue = metadataValue(data, "gym_id");
        String organisationIdValue = metadataValue(data, "organisation_id");

        BigDecimal amount = fromMinor(data.path("amount").asLong(0));
        String currency = data.path("currency").asText("NGN").toUpperCase();
        String reference = data.path("reference").asText();
        String reason = data.path("gateway_response").asText("Payment could not be processed");

        if (membershipId != null) {
            memberMembershipRepository.findById(UUID.fromString(membershipId)).ifPresent(membership -> {
                membership.setStatus(MembershipStatus.PAST_DUE);
                memberMembershipRepository.save(membership);

                MemberInvoice invoice = MemberInvoice.builder()
                        .memberId(membership.getMemberId())
                        .membershipId(membership.getId())
                        .amount(amount)
                        .currency(currency)
                        .status(MemberInvoiceStatus.PAYMENT_FAILED)
                        .description("Payment failed: " + reason)
                        .build();
                invoice.setGymId(membership.getGymId());
                invoice.setOrganisationId(membership.getOrganisationId());
                memberInvoiceRepository.save(invoice);
            });
        }

        if (gymIdValue != null && organisationIdValue != null) {
            eventPublisher.publishEvent(PaymentFailedEvent.builder()
                    .organisationId(UUID.fromString(organisationIdValue))
                    .gymId(UUID.fromString(gymIdValue))
                    .amount(amount)
                    .failureReason(reason)
                    .nextRetryDate(LocalDateTime.now().plusDays(3))
                    .invoiceId(reference)
                    .build());
        }
    }

    private void handleRefundUpdated(JsonNode data, String eventType) {
        String reference = data.path("reference").asText();
        if (reference == null || reference.isBlank()) {
            reference = data.path("id").asText();
        }

        if (reference == null || reference.isBlank()) {
            return;
        }

        paymentRefundRepository.findByProviderRefundId(reference).ifPresent(refund -> {
            if ("refund.processed".equals(eventType)) {
                refund.updateStatus(RefundStatus.SUCCEEDED);
            } else {
                refund.markFailed(data.path("status").asText("refund_failed"));
            }
            paymentRefundRepository.save(refund);
        });
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new DomainException("PAYSTACK_WEBHOOK_INVALID_PAYLOAD", "Invalid Paystack webhook payload");
        }
    }

    private void verifySignature(String payload, String signature) {
        if (!paystackConfig.isConfigured()) {
            throw new DomainException("PAYSTACK_NOT_CONFIGURED", "Paystack is not configured");
        }
        if (signature == null || signature.isBlank()) {
            throw new DomainException("INVALID_WEBHOOK_SIGNATURE", "Missing Paystack signature");
        }

        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            sha512Hmac.init(new SecretKeySpec(paystackConfig.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = toHex(hash);

            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8))) {
                throw new DomainException("INVALID_WEBHOOK_SIGNATURE", "Invalid Paystack signature");
            }
        } catch (DomainException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomainException("INVALID_WEBHOOK_SIGNATURE", "Failed to verify Paystack signature");
        }
    }

    private String resolveProviderEventId(String eventType, JsonNode data, String payload) {
        if (data.hasNonNull("id")) {
            return eventType + ":" + data.get("id").asText();
        }
        if (data.hasNonNull("reference")) {
            return eventType + ":" + data.get("reference").asText();
        }
        return eventType + ":" + Integer.toHexString(payload.hashCode());
    }

    private String metadataValue(JsonNode data, String key) {
        JsonNode metadata = data.path("metadata");
        if (metadata.hasNonNull(key)) {
            return metadata.get(key).asText();
        }
        return null;
    }

    private BigDecimal fromMinor(long amountMinor) {
        return BigDecimal.valueOf(amountMinor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

