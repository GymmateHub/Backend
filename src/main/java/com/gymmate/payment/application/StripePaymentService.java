package com.gymmate.payment.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.payment.api.dto.InvoiceResponse;
import com.gymmate.payment.api.dto.PaymentMethodResponse;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.GymInvoiceRepository;
import com.gymmate.payment.infrastructure.PaymentMethodRepository;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.payment.domain.PaymentMethod;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.subscription.domain.Subscription;
import com.gymmate.subscription.domain.SubscriptionRepository;
import com.gymmate.subscription.domain.SubscriptionTier;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling Stripe platform payments (Gym → GymMate).
 * Manages customer creation, payment methods, subscriptions, and invoices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final StripeConfig stripeConfig;
    private final GymRepository gymRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    /**
     * Create a Stripe customer for a gym.
     */
    @Transactional
    public String createOrGetStripeCustomer(UUID gymId) {
        Gym gym = getGym(gymId);
        Subscription subscription = getSubscription(gymId);

        // Return existing customer ID if present
        if (subscription.getStripeCustomerId() != null) {
            return subscription.getStripeCustomerId();
        }

        validateStripeConfigured();

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(gym.getContactEmail())
                    .setName(gym.getName())
                    .putMetadata("gym_id", gymId.toString())
                    .build();

            Customer customer = Customer.create(params);

            // Update subscription with Stripe customer ID
            subscription.setStripeCustomerId(customer.getId());
            subscriptionRepository.save(subscription);

            log.info("Created Stripe customer {} for gym {}", customer.getId(), gymId);
            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_CUSTOMER_CREATE_FAILED",
                "Failed to create payment profile: " + e.getMessage());
        }
    }

    /**
     * Attach a payment method to a gym's Stripe customer.
     */
    @Transactional
    public PaymentMethodResponse attachPaymentMethod(UUID gymId, String stripePaymentMethodId, boolean setAsDefault) {
        String customerId = createOrGetStripeCustomer(gymId);
        validateStripeConfigured();

        try {
            // Attach payment method to customer
            com.stripe.model.PaymentMethod paymentMethod = com.stripe.model.PaymentMethod.retrieve(stripePaymentMethodId);
            paymentMethod.attach(com.stripe.param.PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build());

            // Set as default if requested
            if (setAsDefault) {
                Customer customer = Customer.retrieve(customerId);
                customer.update(CustomerUpdateParams.builder()
                        .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(stripePaymentMethodId)
                                .build())
                        .build());

                // Clear existing defaults in our database
                paymentMethodRepository.clearDefaultForGym(gymId);
            }

            // Save payment method to our database
            PaymentMethod savedMethod = savePaymentMethod(gymId, paymentMethod, setAsDefault);

            log.info("Attached payment method {} to gym {}", stripePaymentMethodId, gymId);
            return toPaymentMethodResponse(savedMethod);

        } catch (StripeException e) {
            log.error("Failed to attach payment method for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_ATTACH_FAILED",
                "Failed to attach payment method: " + e.getMessage());
        }
    }

    /**
     * Get all payment methods for a gym.
     */
    public List<PaymentMethodResponse> getPaymentMethods(UUID gymId) {
        return paymentMethodRepository.findByGymForPlatform(gymId)
                .stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove a payment method.
     */
    @Transactional
    public void removePaymentMethod(UUID gymId, UUID paymentMethodId) {
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));

        if (!method.getGymId().equals(gymId)) {
            throw new DomainException("PAYMENT_METHOD_ACCESS_DENIED", "You don't have access to this payment method");
        }

        validateStripeConfigured();

        try {
            // Detach from Stripe
            com.stripe.model.PaymentMethod stripeMethod = com.stripe.model.PaymentMethod.retrieve(method.getStripePaymentMethodId());
            stripeMethod.detach();

            // Delete from our database
            paymentMethodRepository.delete(method);

            log.info("Removed payment method {} from gym {}", paymentMethodId, gymId);

        } catch (StripeException e) {
            log.error("Failed to remove payment method: {}", e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_REMOVE_FAILED",
                "Failed to remove payment method: " + e.getMessage());
        }
    }

    /**
     * Create a Stripe subscription for a gym.
     */
    @Transactional
    public void createStripeSubscription(UUID gymId, SubscriptionTier tier, boolean startTrial) {
        String customerId = createOrGetStripeCustomer(gymId);
        Subscription subscription = getSubscription(gymId);

        validateStripeConfigured();

        // Skip if already has Stripe subscription
        if (subscription.getStripeSubscriptionId() != null) {
            log.info("Gym {} already has Stripe subscription {}", gymId, subscription.getStripeSubscriptionId());
            return;
        }

        // Check if tier has a Stripe price ID configured
        // For now, we'll log a warning if not configured (manual setup required)
        if (tier.getStripePriceId() == null || tier.getStripePriceId().isBlank()) {
            log.warn("Tier {} does not have a Stripe price ID configured. Subscription created without Stripe billing.",
                tier.getName());
            return;
        }

        try {
            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(tier.getStripePriceId())
                            .build())
                    .putMetadata("gym_id", gymId.toString())
                    .putMetadata("tier_name", tier.getName());

            // Add trial if requested
            if (startTrial && tier.getTrialDays() != null && tier.getTrialDays() > 0) {
                paramsBuilder.setTrialPeriodDays(tier.getTrialDays().longValue());
            }

            // Create Stripe subscription using fully qualified class name
            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription.create(paramsBuilder.build());

            // Update our subscription with Stripe ID
            subscription.setStripeSubscriptionId(stripeSubData.getId());
            subscriptionRepository.save(subscription);

            log.info("Created Stripe subscription {} for gym {}", stripeSubData.getId(), gymId);

        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CREATE_FAILED",
                "Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * Cancel a Stripe subscription.
     */
    @Transactional
    public void cancelStripeSubscription(UUID gymId, boolean immediate) {
        Subscription subscription = getSubscription(gymId);

        if (subscription.getStripeSubscriptionId() == null) {
            log.info("Gym {} has no Stripe subscription to cancel", gymId);
            return;
        }

        validateStripeConfigured();

        try {
            // Retrieve from Stripe using fully qualified class name
            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());

            if (immediate) {
                stripeSubData.cancel();
                log.info("Immediately cancelled Stripe subscription {} for gym {}",
                    subscription.getStripeSubscriptionId(), gymId);
            } else {
                stripeSubData.update(SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build());
                log.info("Scheduled cancellation of Stripe subscription {} for gym {} at period end",
                    subscription.getStripeSubscriptionId(), gymId);
            }

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CANCEL_FAILED",
                "Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * Get invoices for a gym.
     */
    public List<InvoiceResponse> getInvoices(UUID gymId) {
        Subscription subscription = getSubscription(gymId);

        // Return from our database first
        List<GymInvoice> localInvoices = invoiceRepository.findByGymIdOrderByCreatedAtDesc(gymId);
        if (!localInvoices.isEmpty()) {
            return localInvoices.stream()
                    .map(this::toInvoiceResponse)
                    .collect(Collectors.toList());
        }

        // If no local invoices and we have a Stripe customer, fetch from Stripe
        if (subscription.getStripeCustomerId() != null && stripeConfig.isConfigured()) {
            return fetchInvoicesFromStripe(gymId, subscription.getStripeCustomerId());
        }

        return List.of();
    }

    /**
     * Fetch invoices from Stripe and cache them locally.
     */
    @Transactional
    public List<InvoiceResponse> fetchInvoicesFromStripe(UUID gymId, String customerId) {
        try {
            InvoiceListParams params = InvoiceListParams.builder()
                    .setCustomer(customerId)
                    .setLimit(100L)
                    .build();

            InvoiceCollection invoices = Invoice.list(params);

            return invoices.getData().stream()
                    .map(invoice -> {
                        // Save to local database
                        GymInvoice localInvoice = saveInvoiceFromStripe(gymId, invoice);
                        return toInvoiceResponse(localInvoice);
                    })
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("Failed to fetch invoices from Stripe: {}", e.getMessage());
            return List.of();
        }
    }

    // Helper methods

    private Gym getGym(UUID gymId) {
        return gymRepository.findById(gymId)
                .orElseThrow(() -> new DomainException("GYM_NOT_FOUND", "Gym not found"));
    }

    private Subscription getSubscription(UUID gymId) {
        // TODO: Update to use organisationId instead of gymId
        // For now, we need to get the gym first, then get subscription by organisationId
        Gym gym = getGym(gymId);
        if (gym.getOrganisationId() == null) {
            throw new DomainException("GYM_NO_ORGANISATION", "Gym is not associated with an organisation");
        }
        return subscriptionRepository.findByOrganisationId(gym.getOrganisationId())
                .orElseThrow(() -> new DomainException("SUBSCRIPTION_NOT_FOUND",
                    "No subscription found for this gym's organisation"));
    }

    private void validateStripeConfigured() {
        if (!stripeConfig.isConfigured()) {
            throw new DomainException("STRIPE_NOT_CONFIGURED",
                "Payment processing is not configured. Please contact support.");
        }
    }

    private PaymentMethod savePaymentMethod(UUID gymId, com.stripe.model.PaymentMethod paymentMethod, boolean isDefault) {
        com.stripe.model.PaymentMethod.Card card = paymentMethod.getCard();

        PaymentMethod method = PaymentMethod.builder()
                .ownerType(com.gymmate.payment.domain.PaymentMethodOwnerType.GYM)
                .ownerId(gymId)
                .gymId(gymId)
                .provider("stripe")
                .providerPaymentMethodId(paymentMethod.getId())
                .methodType(com.gymmate.payment.domain.PaymentMethodType.CARD)
                .cardBrand(card != null ? card.getBrand() : null)
                .cardLastFour(card != null ? card.getLast4() : null)
                .cardExpiresMonth(card != null && card.getExpMonth() != null ? card.getExpMonth().intValue() : null)
                .cardExpiresYear(card != null && card.getExpYear() != null ? card.getExpYear().intValue() : null)
                .isDefault(isDefault)
                .build();

        return paymentMethodRepository.save(method);
    }

    private GymInvoice saveInvoiceFromStripe(UUID gymId, Invoice stripeInvoice) {
        // Check if already exists
        return invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElseGet(() -> {
                    GymInvoice invoice = GymInvoice.builder()
                            .gymId(gymId)
                            .stripeInvoiceId(stripeInvoice.getId())
                            .invoiceNumber(stripeInvoice.getNumber())
                            .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue())
                                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP))
                            .currency(stripeInvoice.getCurrency().toUpperCase())
                            .status(InvoiceStatus.fromStripeStatus(stripeInvoice.getStatus()))
                            .description(stripeInvoice.getDescription())
                            .periodStart(stripeInvoice.getPeriodStart() != null ?
                                toLocalDateTime(stripeInvoice.getPeriodStart()) : null)
                            .periodEnd(stripeInvoice.getPeriodEnd() != null ?
                                toLocalDateTime(stripeInvoice.getPeriodEnd()) : null)
                            .dueDate(stripeInvoice.getDueDate() != null ?
                                toLocalDateTime(stripeInvoice.getDueDate()) : null)
                            .paidAt(stripeInvoice.getStatusTransitions() != null &&
                                    stripeInvoice.getStatusTransitions().getPaidAt() != null ?
                                toLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt()) : null)
                            .invoicePdfUrl(stripeInvoice.getInvoicePdf())
                            .hostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl())
                            .build();

                    return invoiceRepository.save(invoice);
                });
    }

    private LocalDateTime toLocalDateTime(Long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private PaymentMethodResponse toPaymentMethodResponse(PaymentMethod method) {
        return PaymentMethodResponse.builder()
                .id(method.getId())
                .type(method.getMethodType() != null ? method.getMethodType().name() : null)
                .cardBrand(method.getCardBrand())
                .lastFour(method.getLastFour())
                .expiryMonth(method.getExpiryMonth())
                .expiryYear(method.getExpiryYear())
                .isDefault(method.getIsDefault())
                .build();
    }

    private InvoiceResponse toInvoiceResponse(GymInvoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus().name())
                .description(invoice.getDescription())
                .periodStart(invoice.getPeriodStart())
                .periodEnd(invoice.getPeriodEnd())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .invoicePdfUrl(invoice.getInvoicePdfUrl())
                .hostedInvoiceUrl(invoice.getHostedInvoiceUrl())
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    /**
     * Process a refund for a payment.
     */
    @Transactional
    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        validateStripeConfigured();

        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId());

            if (request.getAmount() != null) {
                paramsBuilder.setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
            }

            if (request.getReason() != null && !request.getReason().isEmpty()) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            Refund refund = Refund.create(paramsBuilder.build());

            // Save refund to database
            PaymentRefund paymentRefund = PaymentRefund.builder()
                    .gymId(gymId)
                    .stripeRefundId(refund.getId())
                    .stripePaymentIntentId(request.getPaymentIntentId())
                    .stripeChargeId(refund.getCharge())
                    .amount(BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100)))
                    .currency(refund.getCurrency().toUpperCase())
                    .status(RefundStatus.valueOf(refund.getStatus().toUpperCase()))
                    .reason(request.getReason())
                    .receiptNumber(refund.getReceiptNumber())
                    .stripeCreatedAt(toLocalDateTime(refund.getCreated()))
                    .build();

            paymentRefundRepository.save(paymentRefund);

            log.info("Processed refund {} for gym {} on payment {}",
                    refund.getId(), gymId, request.getPaymentIntentId());

            return RefundResponse.builder()
                    .refundId(refund.getId())
                    .paymentIntentId(request.getPaymentIntentId())
                    .amount(paymentRefund.getAmount())
                    .currency(paymentRefund.getCurrency())
                    .status(paymentRefund.getStatus().name())
                    .reason(request.getReason())
                    .createdAt(paymentRefund.getStripeCreatedAt())
                    .build();

        } catch (StripeException e) {
            log.error("Failed to process refund for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_REFUND_FAILED",
                    "Failed to process refund: " + e.getMessage());
        }
    }

    /**
     * Get refund history for a gym.
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory(UUID gymId) {
        return paymentRefundRepository.findByGymIdOrderByCreatedAtDesc(gymId)
                .stream()
                .map(this::toRefundResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific refund.
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefund(UUID gymId, UUID refundId) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new DomainException("REFUND_NOT_FOUND",
                        "Refund not found: " + refundId));

        if (!refund.getGymId().equals(gymId)) {
            throw new DomainException("REFUND_ACCESS_DENIED",
                    "Access denied to refund: " + refundId);
        }

        return toRefundResponse(refund);
    }

    private RefundResponse toRefundResponse(PaymentRefund refund) {
        return RefundResponse.builder()
                .refundId(refund.getStripeRefundId())
                .paymentIntentId(refund.getStripePaymentIntentId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus().name())
                .reason(refund.getReason())
                .createdAt(refund.getStripeCreatedAt())
                .build();
    }
}
