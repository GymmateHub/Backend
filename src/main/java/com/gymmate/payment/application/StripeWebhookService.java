package com.gymmate.payment.application;

import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.GymInvoiceRepository;
import com.gymmate.payment.infrastructure.StripeWebhookEventRepository;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.service.UtilityService;
import com.gymmate.subscription.domain.SubscriptionRepository;
import com.gymmate.subscription.domain.SubscriptionStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import java.util.UUID;

/**
 * Service for handling Stripe webhook events.
 * Processes platform events (for gym subscriptions) and Connect events (for
 * member payments).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookService {

    private final StripeConfig stripeConfig;
    private final StripeWebhookEventRepository webhookEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final StripeConnectService connectService;
    private final PaymentNotificationService notificationService;
    private final UtilityService utilityService;

    /**
     * Process a platform webhook event (for gym subscriptions to GymMate).
     */
    @Transactional
    public void processPlatformWebhook(String payload, String signature) {
        Event event = verifyAndParseEvent(payload, signature, stripeConfig.getWebhookSecret());

        // Check for duplicate processing
        if (webhookEventRepository.existsByStripeEventId(event.getId())) {
            log.info("Webhook event {} already processed, skipping", event.getId());
            return;
        }

        // Save event for tracking
        StripeWebhookEvent webhookEvent = StripeWebhookEvent.builder()
                .stripeEventId(event.getId())
                .eventType(event.getType())
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);

        try {
            handlePlatformEvent(event);
            webhookEvent.markProcessed();
        } catch (Exception e) {
            log.error("Failed to process webhook event {}: {}", event.getId(), e.getMessage());
            webhookEvent.markFailed(e.getMessage());
        }

        webhookEventRepository.save(webhookEvent);
    }

    /**
     * Process a Connect webhook event (for member payments to gyms).
     */
    @Transactional
    public void processConnectWebhook(String payload, String signature) {
        Event event = verifyAndParseEvent(payload, signature, stripeConfig.getConnectWebhookSecret());

        // Check for duplicate processing
        if (webhookEventRepository.existsByStripeEventId(event.getId())) {
            log.info("Connect webhook event {} already processed, skipping", event.getId());
            return;
        }

        // Save event for tracking
        StripeWebhookEvent webhookEvent = StripeWebhookEvent.builder()
                .stripeEventId(event.getId())
                .eventType(event.getType())
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);

        try {
            handleConnectEvent(event);
            webhookEvent.markProcessed();
        } catch (Exception e) {
            log.error("Failed to process Connect webhook event {}: {}", event.getId(), e.getMessage());
            webhookEvent.markFailed(e.getMessage());
        }

        webhookEventRepository.save(webhookEvent);
    }

    /**
     * Handle platform events for gym subscriptions.
     */
    private void handlePlatformEvent(Event event) {
        String eventType = event.getType();
        log.info("Processing platform webhook event: {} ({})", eventType, event.getId());

        switch (eventType) {
            case "customer.subscription.created":
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;

            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;

            case "customer.subscription.trial_will_end":
                handleTrialWillEnd(event);
                break;

            case "invoice.paid":
                handleInvoicePaid(event);
                break;

            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;

            case "invoice.created":
            case "invoice.updated":
                handleInvoiceUpdated(event);
                break;

            default:
                log.debug("Unhandled platform event type: {}", eventType);
        }
    }

    /**
     * Handle Connect events for gym accounts.
     */
    private void handleConnectEvent(Event event) {
        String eventType = event.getType();
        log.info("Processing Connect webhook event: {} ({})", eventType, event.getId());

        switch (eventType) {
            case "account.updated":
                handleAccountUpdated(event);
                break;

            case "account.application.deauthorized":
                handleAccountDeauthorized(event);
                break;

            case "payment_intent.succeeded":
                handleConnectPaymentSucceeded(event);
                break;

            case "payment_intent.payment_failed":
                handleConnectPaymentFailed(event);
                break;

            default:
                log.debug("Unhandled Connect event type: {}", eventType);
        }
    }

    // Platform event handlers

    private void handleSubscriptionUpdated(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event,
                com.stripe.model.Subscription.class);
        if (stripeSubscription == null)
            return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .ifPresent(subscription -> {
                    // Update status
                    subscription.setStatus(mapStripeStatus(stripeSubscription.getStatus()));

                    // Update period dates - Stripe SDK v31+ access via raw JSON object
                    try {
                        com.google.gson.JsonObject rawJson = stripeSubscription.getRawJsonObject();
                        if (rawJson.has("current_period_start") && !rawJson.get("current_period_start").isJsonNull()) {
                            subscription.setCurrentPeriodStart(utilityService
                                    .secondsToLocalDateTime(rawJson.get("current_period_start").getAsLong()));
                        }
                        if (rawJson.has("current_period_end") && !rawJson.get("current_period_end").isJsonNull()) {
                            subscription.setCurrentPeriodEnd(utilityService
                                    .secondsToLocalDateTime(rawJson.get("current_period_end").getAsLong()));
                        }
                        if (rawJson.has("trial_start") && !rawJson.get("trial_start").isJsonNull()) {
                            subscription.setTrialStart(
                                    utilityService.secondsToLocalDateTime(rawJson.get("trial_start").getAsLong()));
                        }
                        if (rawJson.has("trial_end") && !rawJson.get("trial_end").isJsonNull()) {
                            subscription.setTrialEnd(
                                    utilityService.secondsToLocalDateTime(rawJson.get("trial_end").getAsLong()));
                        }
                        if (rawJson.has("cancel_at_period_end")) {
                            subscription.setCancelAtPeriodEnd(rawJson.get("cancel_at_period_end").getAsBoolean());
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse subscription period dates: {}", e.getMessage());
                    }

                    subscriptionRepository.save(subscription);
                    log.info("Updated subscription {} status to {}", subscription.getId(), subscription.getStatus());
                });
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event,
                com.stripe.model.Subscription.class);
        if (stripeSubscription == null)
            return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .ifPresent(subscription -> {
                    subscription.setStatus(SubscriptionStatus.CANCELLED);
                    subscription.setCancelledAt(LocalDateTime.now());
                    subscriptionRepository.save(subscription);
                    log.info("Subscription {} marked as cancelled", subscription.getId());

                    // Send cancellation notification
                    notificationService.sendSubscriptionCancelledNotification(
                            subscription.getOrganisationId(),
                            subscription.getCurrentPeriodEnd());
                });
    }

    private void handleTrialWillEnd(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event,
                com.stripe.model.Subscription.class);
        if (stripeSubscription == null)
            return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .ifPresent(subscription -> {
                    log.info("Trial ending soon for subscription {}", subscription.getId());
                    // Send trial ending notification email
                    notificationService.sendTrialEndingReminder(subscription.getOrganisationId(), subscription);
                });
    }

    private void handleInvoicePaid(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null)
            return;

        // Update or create invoice record
        GymInvoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElseGet(() -> createInvoiceFromStripe(stripeInvoice));

        invoice.markPaid(stripeInvoice.getStatusTransitions() != null &&
                stripeInvoice.getStatusTransitions().getPaidAt() != null
                        ? utilityService.secondsToLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt())
                        : LocalDateTime.now());

        invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid", stripeInvoice.getId());

        // Send payment success notification
        if (invoice.getOrganisationId() != null) {
            notificationService.sendPaymentSuccessNotification(invoice.getOrganisationId(), invoice);
        }

        // Update subscription status if it was past due
        // In Stripe SDK v31+, access subscription via raw JSON
        try {
            com.google.gson.JsonObject rawJson = stripeInvoice.getRawJsonObject();
            if (rawJson.has("subscription") && !rawJson.get("subscription").isJsonNull()) {
                String subscriptionId = rawJson.get("subscription").getAsString();
                subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
                        .ifPresent(subscription -> {
                            if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
                                subscription.setStatus(SubscriptionStatus.ACTIVE);
                                subscriptionRepository.save(subscription);
                                log.info("Subscription {} reactivated after payment", subscription.getId());
                            }
                        });
            }
        } catch (Exception e) {
            log.warn("Failed to get subscription from invoice: {}", e.getMessage());
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null)
            return;

        // Update invoice status
        invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .ifPresent(invoice -> {
                    invoice.markFailed();
                    invoiceRepository.save(invoice);
                });

        // Update subscription to past_due and send notification
        // In Stripe SDK v31+, access subscription via raw JSON
        try {
            com.google.gson.JsonObject rawJson = stripeInvoice.getRawJsonObject();
            if (rawJson.has("subscription") && !rawJson.get("subscription").isJsonNull()) {
                String subscriptionId = rawJson.get("subscription").getAsString();
                subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
                        .ifPresent(subscription -> {
                            subscription.markPastDue();
                            subscriptionRepository.save(subscription);
                            log.warn("Subscription {} marked as past due due to payment failure", subscription.getId());

                            // Get failure reason and next retry date
                            String failureReason = "Payment could not be processed";
                            LocalDateTime nextRetryDate = LocalDateTime.now().plusDays(3);

                            try {
                                if (rawJson.has("next_payment_attempt")
                                        && !rawJson.get("next_payment_attempt").isJsonNull()) {
                                    nextRetryDate = utilityService
                                            .secondsToLocalDateTime(rawJson.get("next_payment_attempt").getAsLong());
                                }
                            } catch (Exception ex) {
                                log.debug("Could not parse next_payment_attempt: {}", ex.getMessage());
                            }

                            // Send payment failed notification
                            BigDecimal amount = BigDecimal.valueOf(stripeInvoice.getAmountDue())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                            notificationService.sendPaymentFailedNotification(
                                    subscription.getOrganisationId(),
                                    amount,
                                    failureReason,
                                    nextRetryDate);
                        });
            }
        } catch (Exception e) {
            log.warn("Failed to get subscription from invoice: {}", e.getMessage());
        }
    }

    private void handleInvoiceUpdated(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null)
            return;

        // Only process if we have a customer to link to
        if (stripeInvoice.getCustomer() == null)
            return;

        // Find subscription by customer ID and create/update invoice
        subscriptionRepository.findByStripeCustomerId(stripeInvoice.getCustomer())
                .ifPresent(subscription -> {
                    // TODO: GymInvoice should be renamed to OrganisationInvoice since subscriptions
                    // are now org-level
                    GymInvoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                            .orElseGet(() -> createInvoiceFromStripe(stripeInvoice, null));

                    invoice.setStatus(InvoiceStatus.fromStripeStatus(stripeInvoice.getStatus()));
                    invoice.setInvoicePdfUrl(stripeInvoice.getInvoicePdf());
                    invoice.setHostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl());

                    invoiceRepository.save(invoice);
                });
    }

    // Connect event handlers

    private void handleAccountUpdated(Event event) {
        Account account = extractEventObject(event, Account.class);
        if (account == null)
            return;

        connectService.handleAccountUpdated(account.getId());
    }

    private void handleAccountDeauthorized(Event event) {
        Account account = extractEventObject(event, Account.class);
        if (account == null)
            return;

        String gymIdStr = account.getMetadata().get("gym_id");
        if (gymIdStr != null) {
            log.warn("Gym {} deauthorized their Stripe Connect account", gymIdStr);
            // TODO: Handle account deauthorization - notify gym owner
        }
    }

    private void handleConnectPaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = extractEventObject(event, PaymentIntent.class);
        if (paymentIntent == null)
            return;

        log.info("Connect payment succeeded: {} for amount {}",
                paymentIntent.getId(), paymentIntent.getAmount());
        // TODO: Update member membership payment status
    }

    private void handleConnectPaymentFailed(Event event) {
        PaymentIntent paymentIntent = extractEventObject(event, PaymentIntent.class);
        if (paymentIntent == null)
            return;

        log.warn("Connect payment failed: {} - {}",
                paymentIntent.getId(), paymentIntent.getLastPaymentError());
        // TODO: Handle member payment failure - notify gym and member
    }

    // Helper methods

    private Event verifyAndParseEvent(String payload, String signature, String webhookSecret) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new DomainException("WEBHOOK_NOT_CONFIGURED", "Webhook secret not configured");
        }

        try {
            return Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature: {}", e.getMessage());
            throw new DomainException("INVALID_WEBHOOK_SIGNATURE", "Invalid webhook signature");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extractEventObject(Event event, @SuppressWarnings("unused") Class<T> clazz) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (T) deserializer.getObject().get();
        }
        log.warn("Failed to deserialize event object for event {}", event.getId());
        return null;
    }

    private GymInvoice createInvoiceFromStripe(Invoice stripeInvoice) {
        String orgIdStr = stripeInvoice.getMetadata().get("organisation_id");
        UUID organisationId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;
        return createInvoiceFromStripe(stripeInvoice, organisationId);
    }

    private GymInvoice createInvoiceFromStripe(Invoice stripeInvoice, UUID organisationId) {
        if (organisationId == null) {
            // Try to find organisation by customer ID
            var subscription = subscriptionRepository.findByStripeCustomerId(stripeInvoice.getCustomer());
            if (subscription.isPresent()) {
                organisationId = subscription.get().getOrganisationId();
            }
        }

        return GymInvoice.builder()
                .organisationId(organisationId)
                .stripeInvoiceId(stripeInvoice.getId())
                .invoiceNumber(stripeInvoice.getNumber())
                .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP))
                .currency(stripeInvoice.getCurrency() != null ? stripeInvoice.getCurrency().toUpperCase() : "USD")
                .status(InvoiceStatus.fromStripeStatus(stripeInvoice.getStatus()))
                .description(stripeInvoice.getDescription())
                .periodStart(stripeInvoice.getPeriodStart() != null
                        ? utilityService.secondsToLocalDateTime(stripeInvoice.getPeriodStart())
                        : null)
                .periodEnd(stripeInvoice.getPeriodEnd() != null
                        ? utilityService.secondsToLocalDateTime(stripeInvoice.getPeriodEnd())
                        : null)
                .dueDate(stripeInvoice.getDueDate() != null
                        ? utilityService.secondsToLocalDateTime(stripeInvoice.getDueDate())
                        : null)
                .invoicePdfUrl(stripeInvoice.getInvoicePdf())
                .hostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl())
                .build();
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIAL;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled", "cancelled" -> SubscriptionStatus.CANCELLED;
            case "unpaid" -> SubscriptionStatus.SUSPENDED;
            case "incomplete", "incomplete_expired" -> SubscriptionStatus.EXPIRED;
            default -> SubscriptionStatus.ACTIVE;
        };
    }

}
