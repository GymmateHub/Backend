package com.gymmate.payment.application;

import com.gymmate.notification.events.ChargeDisputedEvent;
import com.gymmate.notification.events.ChargeRefundedEvent;
import com.gymmate.notification.events.PaymentFailedEvent;
import com.gymmate.notification.events.PaymentSuccessEvent;
import com.gymmate.notification.events.SubscriptionPausedEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.GymInvoiceRepository;
import com.gymmate.payment.infrastructure.StripeWebhookEventRepository;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.service.UtilityService;
import com.gymmate.subscription.domain.SubscriptionRepository;
import com.gymmate.shared.constants.SubscriptionStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UtilityService utilityService;
    private final NotificationService notificationService;
    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final MemberInvoiceRepository memberInvoiceRepository;

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

            case "customer.subscription.paused":
                handleSubscriptionPaused(event);
                break;

            case "charge.disputed":
                handleChargeDisputed(event);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
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

            case "charge.disputed":
                handleChargeDisputed(event);
                break;

            case "charge.refunded":
                handleChargeRefunded(event);
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

                    // Note: Subscription cancelled notification can be added as a new event type if needed
                    // For now, we'll keep the direct notification call or implement SubscriptionCancelledEvent
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

                    // Publish subscription expiring event
                    if (subscription.getTrialEnd() != null) {
                        long daysUntilExpiry = java.time.Duration.between(
                                LocalDateTime.now(),
                                subscription.getTrialEnd()
                        ).toDays();

                        com.gymmate.notification.events.SubscriptionExpiringEvent expiringEvent =
                                com.gymmate.notification.events.SubscriptionExpiringEvent.builder()
                                .organisationId(subscription.getOrganisationId())
                                .subscriptionId(subscription.getId())
                                .tierName(subscription.getTier().getDisplayName())
                                .price(subscription.getTier().getPrice())
                                .expiresAt(subscription.getTrialEnd())
                                .daysUntilExpiry((int) daysUntilExpiry)
                                .build();

                        eventPublisher.publishEvent(expiringEvent);
                        log.info("Published SubscriptionExpiringEvent for organisation {}", subscription.getOrganisationId());
                    }
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

        // Publish payment success event
        if (invoice.getOrganisationId() != null) {
            PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                    .organisationId(invoice.getOrganisationId())
                    .gymId(invoice.getOrganisationId()) // Using org ID for now
                    .amount(invoice.getAmount())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceUrl(invoice.getHostedInvoiceUrl())
                    .periodEnd(invoice.getPeriodEnd())
                    .build();

            eventPublisher.publishEvent(successEvent);
            log.info("Published PaymentSuccessEvent for organisation {}", invoice.getOrganisationId());
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

                            // Publish payment failed event
                            BigDecimal amount = BigDecimal.valueOf(stripeInvoice.getAmountDue())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                            PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                                    .organisationId(subscription.getOrganisationId())
                                    .gymId(subscription.getOrganisationId()) // Using org ID since gyms are org-level now
                                    .amount(amount)
                                    .failureReason(failureReason)
                                    .nextRetryDate(nextRetryDate)
                                    .invoiceId(stripeInvoice.getId())
                                    .build();

                            eventPublisher.publishEvent(paymentFailedEvent);
                            log.info("Published PaymentFailedEvent for organisation {}", subscription.getOrganisationId());
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
            UUID gymId = UUID.fromString(gymIdStr);
            log.warn("Gym {} deauthorized their Stripe Connect account", gymIdStr);

            // Clear Connect fields on the gym via the connect service
            connectService.handleAccountDeauthorized(gymId);

            // Notify gym owner via the notification system
            String orgIdStr = account.getMetadata().get("organisation_id");
            UUID organisationId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;

            if (organisationId != null) {
                notificationService.createAndBroadcast(
                        "⚠️ Stripe Account Disconnected",
                        "Your Stripe Connect account has been disconnected. Member payments will not be processed until you reconnect.",
                        organisationId,
                        NotificationPriority.CRITICAL,
                        "STRIPE_CONNECT_DEAUTHORIZED",
                        java.util.Map.of("gymId", gymId.toString()));
            }
        }
    }

    private void handleConnectPaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = extractEventObject(event, PaymentIntent.class);
        if (paymentIntent == null)
            return;

        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = paymentIntent.getCurrency() != null ? paymentIntent.getCurrency().toUpperCase() : "USD";

        log.info("Connect payment succeeded: {} for amount {} {}",
                paymentIntent.getId(), amount, currency);

        // Look up the membership via metadata on the PaymentIntent
        var metadata = paymentIntent.getMetadata();
        String membershipId = metadata != null ? metadata.get("membership_id") : null;
        String memberId = metadata != null ? metadata.get("member_id") : null;
        String gymIdStr = metadata != null ? metadata.get("gym_id") : null;

        if (membershipId != null) {
            memberMembershipRepository.findById(UUID.fromString(membershipId))
                    .ifPresent(membership -> {
                        // Mark membership active if it was pending/past_due
                        if (membership.getStatus() == MembershipStatus.EXPIRED
                                || membership.getStatus() == MembershipStatus.CANCELLED) {
                            membership.setStatus(MembershipStatus.ACTIVE);
                            memberMembershipRepository.save(membership);
                            log.info("Membership {} reactivated after payment", membershipId);
                        }

                        // Create a paid invoice record
                        MemberInvoice invoice = MemberInvoice.builder()
                                .memberId(membership.getMemberId())
                                .membershipId(membership.getId())
                                .amount(amount)
                                .currency(currency)
                                .status(MemberInvoiceStatus.PAID)
                                .description("Membership payment via Stripe Connect")
                                .paidAt(LocalDateTime.now())
                                .build();
                        invoice.setGymId(membership.getGymId());
                        invoice.setOrganisationId(membership.getOrganisationId());
                        memberInvoiceRepository.save(invoice);
                    });
        }

        // Publish payment success event for notifications
        if (gymIdStr != null) {
            UUID gymId = UUID.fromString(gymIdStr);
            UUID organisationId = null;
            if (membershipId != null) {
                organisationId = memberMembershipRepository.findById(UUID.fromString(membershipId))
                        .map(m -> m.getOrganisationId()).orElse(null);
            }

            if (organisationId != null) {
                PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                        .organisationId(organisationId)
                        .gymId(gymId)
                        .amount(amount)
                        .invoiceNumber(paymentIntent.getId())
                        .build();
                eventPublisher.publishEvent(successEvent);
            }
        }
    }

    private void handleConnectPaymentFailed(Event event) {
        PaymentIntent paymentIntent = extractEventObject(event, PaymentIntent.class);
        if (paymentIntent == null)
            return;

        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String failureMessage = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Payment could not be processed";

        log.warn("Connect payment failed: {} - {}", paymentIntent.getId(), failureMessage);

        // Look up the membership via metadata
        var metadata = paymentIntent.getMetadata();
        String membershipId = metadata != null ? metadata.get("membership_id") : null;
        String gymIdStr = metadata != null ? metadata.get("gym_id") : null;

        UUID organisationId = null;

        if (membershipId != null) {
            var membershipOpt = memberMembershipRepository.findById(UUID.fromString(membershipId));
            if (membershipOpt.isPresent()) {
                var membership = membershipOpt.get();
                organisationId = membership.getOrganisationId();

                // Mark membership as past due so access can be restricted
                membership.setStatus(MembershipStatus.PAST_DUE);
                memberMembershipRepository.save(membership);
                log.warn("Membership {} marked as PAST_DUE due to payment failure", membershipId);

                // Create a failed invoice record
                MemberInvoice invoice = MemberInvoice.builder()
                        .memberId(membership.getMemberId())
                        .membershipId(membership.getId())
                        .amount(amount)
                        .currency(paymentIntent.getCurrency() != null ? paymentIntent.getCurrency().toUpperCase() : "USD")
                        .status(MemberInvoiceStatus.PAYMENT_FAILED)
                        .description("Payment failed: " + failureMessage)
                        .build();
                invoice.setGymId(membership.getGymId());
                invoice.setOrganisationId(membership.getOrganisationId());
                memberInvoiceRepository.save(invoice);
            }
        }

        // Publish payment failed event to notify gym owner and member
        if (gymIdStr != null && organisationId != null) {
            UUID gymId = UUID.fromString(gymIdStr);
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .amount(amount)
                    .failureReason(failureMessage)
                    .nextRetryDate(LocalDateTime.now().plusDays(3))
                    .invoiceId(paymentIntent.getId())
                    .build();
            eventPublisher.publishEvent(failedEvent);
            log.info("Published PaymentFailedEvent for Connect payment failure on gym {}", gymIdStr);
        }
    }

    // Helper methods

    /**
     * Handle subscription paused event.
     * Updates subscription status to PAUSED and notifies the organisation owner.
     */
    private void handleSubscriptionPaused(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event,
                com.stripe.model.Subscription.class);
        if (stripeSubscription == null)
            return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSubscription.getId())
                .ifPresent(subscription -> {
                    subscription.setStatus(SubscriptionStatus.PAUSED);
                    subscriptionRepository.save(subscription);
                    log.info("Subscription {} paused", subscription.getId());

                    SubscriptionPausedEvent pausedEvent = SubscriptionPausedEvent.builder()
                            .organisationId(subscription.getOrganisationId())
                            .subscriptionId(subscription.getId())
                            .tierName(subscription.getTier() != null
                                    ? subscription.getTier().getDisplayName() : "Unknown")
                            .pausedAt(LocalDateTime.now())
                            .build();

                    eventPublisher.publishEvent(pausedEvent);
                    log.info("Published SubscriptionPausedEvent for organisation {}",
                            subscription.getOrganisationId());
                });
    }

    /**
     * Handle charge disputed event (chargeback).
     * Logs the dispute, notifies the organisation owner via a CRITICAL notification.
     */
    private void handleChargeDisputed(Event event) {
        Dispute dispute = extractEventObject(event, Dispute.class);
        if (dispute == null)
            return;

        BigDecimal amount = BigDecimal.valueOf(dispute.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = dispute.getCurrency() != null ? dispute.getCurrency().toUpperCase() : "USD";
        String reason = dispute.getReason() != null ? dispute.getReason() : "not_provided";

        log.warn("Charge disputed: {} for amount {} {} — reason: {}",
                dispute.getId(), amount, currency, reason);

        // Resolve organisation from the charge's metadata or customer
        UUID organisationId = resolveOrganisationFromDispute(dispute);

        if (organisationId != null) {
            ChargeDisputedEvent disputedEvent = ChargeDisputedEvent.builder()
                    .organisationId(organisationId)
                    .amount(amount)
                    .currency(currency)
                    .disputeId(dispute.getId())
                    .disputeReason(reason)
                    .paymentIntentId(dispute.getPaymentIntent())
                    .build();

            eventPublisher.publishEvent(disputedEvent);
            log.info("Published ChargeDisputedEvent for organisation {}", organisationId);
        } else {
            // Fallback: create a system-level notification via direct service call
            log.warn("Could not resolve organisation for dispute {}. Logging only.", dispute.getId());
        }
    }

    /**
     * Handle charge refunded event (full or partial refund processed).
     * Updates invoice status and notifies the organisation owner.
     */
    private void handleChargeRefunded(Event event) {
        Charge charge = extractEventObject(event, Charge.class);
        if (charge == null)
            return;

        BigDecimal amountRefunded = BigDecimal.valueOf(charge.getAmountRefunded())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = charge.getCurrency() != null ? charge.getCurrency().toUpperCase() : "USD";

        log.info("Charge refunded: {} — refunded amount {} {}",
                charge.getId(), amountRefunded, currency);

        // Resolve organisation from charge metadata or associated invoice
        UUID organisationId = resolveOrganisationFromCharge(charge);

        // Update any associated invoice status to REFUNDED if fully refunded
        if (charge.getRefunded() != null && charge.getRefunded()) {
            try {
                com.google.gson.JsonObject raw = charge.getRawJsonObject();
                if (raw != null && raw.has("invoice") && !raw.get("invoice").isJsonNull()) {
                    String invoiceId = raw.get("invoice").getAsString();
                    invoiceRepository.findByStripeInvoiceId(invoiceId)
                            .ifPresent(invoice -> {
                                invoice.setStatus(InvoiceStatus.REFUNDED);
                                invoiceRepository.save(invoice);
                                log.info("Invoice {} marked as REFUNDED", invoiceId);
                            });
                }
            } catch (Exception e) {
                log.debug("Could not resolve invoice from charge for refund: {}", e.getMessage());
            }
        }

        if (organisationId != null) {
            // Determine refund reason from the latest refund object
            String refundReason = "Refund processed";
            String latestRefundId = null;
            if (charge.getRefunds() != null && charge.getRefunds().getData() != null
                    && !charge.getRefunds().getData().isEmpty()) {
                var latestRefund = charge.getRefunds().getData().get(0);
                latestRefundId = latestRefund.getId();
                if (latestRefund.getReason() != null) {
                    refundReason = latestRefund.getReason();
                }
            }

            ChargeRefundedEvent refundedEvent = ChargeRefundedEvent.builder()
                    .organisationId(organisationId)
                    .amount(amountRefunded)
                    .currency(currency)
                    .refundId(latestRefundId)
                    .paymentIntentId(charge.getPaymentIntent())
                    .reason(refundReason)
                    .build();

            eventPublisher.publishEvent(refundedEvent);
            log.info("Published ChargeRefundedEvent for organisation {}", organisationId);
        }
    }

    /**
     * Resolve organisation ID from a Dispute object.
     * Tries metadata on the payment intent, then falls back to customer lookup.
     */
    private UUID resolveOrganisationFromDispute(Dispute dispute) {
        // Try metadata
        if (dispute.getMetadata() != null) {
            String orgIdStr = dispute.getMetadata().get("organisation_id");
            if (orgIdStr != null) {
                return UUID.fromString(orgIdStr);
            }
        }

        // Fallback: resolve from customer via subscription
        try {
            com.google.gson.JsonObject raw = dispute.getRawJsonObject();
            if (raw != null && raw.has("charge") && !raw.get("charge").isJsonNull()) {
                // The charge field may contain the customer; resolve via subscription
                String customer = raw.has("customer") && !raw.get("customer").isJsonNull()
                        ? raw.get("customer").getAsString() : null;
                if (customer != null) {
                    return subscriptionRepository.findByStripeCustomerId(customer)
                            .map(s -> s.getOrganisationId()).orElse(null);
                }
            }
        } catch (Exception e) {
            log.debug("Failed to resolve organisation from dispute: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Resolve organisation ID from a Charge object.
     * Tries metadata, then customer lookup via subscription.
     */
    private UUID resolveOrganisationFromCharge(Charge charge) {
        // Try metadata
        if (charge.getMetadata() != null) {
            String orgIdStr = charge.getMetadata().get("organisation_id");
            if (orgIdStr != null) {
                return UUID.fromString(orgIdStr);
            }
        }

        // Fallback: resolve from customer via subscription
        if (charge.getCustomer() != null) {
            var result = subscriptionRepository.findByStripeCustomerId(charge.getCustomer())
                    .map(s -> s.getOrganisationId()).orElse(null);
            if (result != null) return result;
        }

        // Fallback: resolve from invoice via raw JSON (Stripe SDK v31+)
        try {
            com.google.gson.JsonObject raw = charge.getRawJsonObject();
            if (raw != null && raw.has("invoice") && !raw.get("invoice").isJsonNull()) {
                String invoiceId = raw.get("invoice").getAsString();
                return invoiceRepository.findByStripeInvoiceId(invoiceId)
                        .map(GymInvoice::getOrganisationId).orElse(null);
            }
        } catch (Exception e) {
            log.debug("Could not resolve invoice from charge: {}", e.getMessage());
        }

        return null;
    }

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
            case "paused" -> SubscriptionStatus.PAUSED;
            case "canceled", "cancelled" -> SubscriptionStatus.CANCELLED;
            case "unpaid" -> SubscriptionStatus.SUSPENDED;
            case "incomplete", "incomplete_expired" -> SubscriptionStatus.EXPIRED;
            default -> SubscriptionStatus.ACTIVE;
        };
    }

}
