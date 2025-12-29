package com.gymmate.payment.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.payment.api.dto.*;
import com.gymmate.payment.domain.GymInvoice;
import com.gymmate.payment.domain.InvoiceStatus;
import com.gymmate.payment.domain.PaymentMethodType;
import com.gymmate.payment.domain.PaymentRefund;
import com.gymmate.payment.domain.RefundStatus;
import com.gymmate.payment.infrastructure.*;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.gym.application.GymService;
import com.gymmate.shared.service.UtilityService;
import com.gymmate.subscription.domain.GymSubscription;
import com.gymmate.subscription.infrastructure.GymSubscriptionRepository;
import com.gymmate.subscription.domain.SubscriptionTier;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
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
    private final GymSubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final PaymentRefundRepository refundRepository;
    private final UtilityService utilityService;
    private final GymService gymService;

    /**
     * Create a Stripe customer for a gym.
     */
    @Transactional
    public String createOrGetStripeCustomer(UUID gymId) {
        Gym gym = gymService.getGymById(gymId);
        GymSubscription subscription = getSubscription(gymId);

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
            com.stripe.model.PaymentMethod stripePaymentMethod = com.stripe.model.PaymentMethod.retrieve(stripePaymentMethodId);
            stripePaymentMethod.attach(PaymentMethodAttachParams.builder()
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
            com.gymmate.payment.domain.PaymentMethod savedMethod = savePaymentMethod(gymId, stripePaymentMethod, setAsDefault);

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
        com.gymmate.payment.domain.PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));

        if (!method.getGymId().equals(gymId) || !method.isGymPaymentMethod()) {
            throw new DomainException("PAYMENT_METHOD_ACCESS_DENIED", "You don't have access to this payment method");
        }

        validateStripeConfigured();

        try {
            // Detach from Stripe
            com.stripe.model.PaymentMethod stripeMethod = com.stripe.model.PaymentMethod.retrieve(method.getProviderPaymentMethodId());
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
        GymSubscription subscription = getSubscription(gymId);

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

            Subscription stripeSubscription = Subscription.create(paramsBuilder.build());

            // Update our subscription with Stripe ID
            subscription.setStripeSubscriptionId(stripeSubscription.getId());
            subscriptionRepository.save(subscription);

            log.info("Created Stripe subscription {} for gym {}", stripeSubscription.getId(), gymId);

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
        GymSubscription subscription = getSubscription(gymId);

        if (subscription.getStripeSubscriptionId() == null) {
            log.info("Gym {} has no Stripe subscription to cancel", gymId);
            return;
        }

        validateStripeConfigured();

        try {
            Subscription stripeSubscription = Subscription.retrieve(subscription.getStripeSubscriptionId());

            if (immediate) {
                stripeSubscription.cancel();
                log.info("Immediately cancelled Stripe subscription {} for gym {}",
                    subscription.getStripeSubscriptionId(), gymId);
            } else {
                stripeSubscription.update(SubscriptionUpdateParams.builder()
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
        GymSubscription subscription = getSubscription(gymId);

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

  /**
     * Process a refund for a payment.
     *
     * @param gymId The gym ID requesting the refund
     * @param request The refund request containing payment intent ID, amount, and reason
     * @return RefundResponse with refund details
     */
    @Transactional
    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        validateStripeConfigured();

        // Verify the gym exists
      gymService.getGymById(gymId);

        try {
            // Retrieve the payment intent to verify it exists and get details
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());

            // Verify the payment intent is in a refundable state
            if (!"succeeded".equals(paymentIntent.getStatus())) {
                throw new DomainException("PAYMENT_NOT_REFUNDABLE",
                    "Payment cannot be refunded. Current status: " + paymentIntent.getStatus());
            }

            // Build refund parameters
            RefundCreateParams.Builder refundParamsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getPaymentIntentId())
                    .putMetadata("gym_id", gymId.toString())
                    .putMetadata("requested_by", "gym_admin");

            // Set amount if partial refund (convert dollars to cents)
            if (request.getAmount() != null) {
                long amountInCents = request.getAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

                // Validate amount doesn't exceed original payment
                if (amountInCents > paymentIntent.getAmount()) {
                    throw new DomainException("REFUND_AMOUNT_EXCEEDS_PAYMENT",
                        "Refund amount cannot exceed the original payment amount");
                }

                refundParamsBuilder.setAmount(amountInCents);
            }

            // Set reason if provided
            if (request.getReason() != null && !request.getReason().isBlank()) {
                // Map to Stripe's allowed reason values or use metadata
                RefundCreateParams.Reason stripeReason = mapToStripeReason(request.getReason());
                if (stripeReason != null) {
                    refundParamsBuilder.setReason(stripeReason);
                }
                refundParamsBuilder.putMetadata("custom_reason", request.getReason());
            }

            // Create the refund
            Refund refund = Refund.create(refundParamsBuilder.build());

            log.info("Created refund {} for payment intent {} (gym: {})",
                refund.getId(), request.getPaymentIntentId(), gymId);

            // Convert amount from cents to dollars
            BigDecimal refundAmount = BigDecimal.valueOf(refund.getAmount())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            // Save refund record for audit and analytics
            PaymentRefund paymentRefund = saveRefundRecord(gymId, refund, request);

            return RefundResponse.builder()
                    .refundId(refund.getId())
                    .paymentIntentId(request.getPaymentIntentId())
                    .amount(refundAmount)
                    .currency(refund.getCurrency().toUpperCase())
                    .status(refund.getStatus())
                    .reason(request.getReason())
                    .createdAt(utilityService.secondsToLocalDateTime(refund.getCreated()))
                    .build();

        } catch (StripeException e) {
            log.error("Failed to process refund for payment intent {}: {}",
                request.getPaymentIntentId(), e.getMessage());
            throw new DomainException("STRIPE_REFUND_FAILED",
                "Failed to process refund: " + e.getMessage());
        }
    }

    /**
     * Save a refund record to the database for audit and analytics.
     */
    private PaymentRefund saveRefundRecord(UUID gymId, Refund stripeRefund, RefundRequest request) {
        // Check if refund already exists (idempotency)
        return refundRepository.findByStripeRefundId(stripeRefund.getId())
                .orElseGet(() -> {
                    BigDecimal amount = BigDecimal.valueOf(stripeRefund.getAmount())
                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

                    // Try to find subscription for this gym
                    UUID subscriptionId = subscriptionRepository.findByGymId(gymId)
                            .map(GymSubscription::getId)
                            .orElse(null);

                    PaymentRefund refundRecord = PaymentRefund.builder()
                            .gymId(gymId)
                            .stripeRefundId(stripeRefund.getId())
                            .stripePaymentIntentId(request.getPaymentIntentId())
                            .stripeChargeId(stripeRefund.getCharge())
                            .amount(amount)
                            .currency(stripeRefund.getCurrency() != null ? stripeRefund.getCurrency().toUpperCase() : "USD")
                            .status(RefundStatus.fromStripeStatus(stripeRefund.getStatus()))
                            .reason(stripeRefund.getReason())
                            .customReason(request.getReason())
                            .subscriptionId(subscriptionId)
                            .requestedByType("user")
                            .receiptNumber(stripeRefund.getReceiptNumber())
                            .stripeCreatedAt(utilityService.secondsToLocalDateTime(stripeRefund.getCreated()))
                            .build();

                    PaymentRefund saved = refundRepository.save(refundRecord);
                    log.info("Saved refund record {} for Stripe refund {}", saved.getId(), stripeRefund.getId());
                    return saved;
                });
    }

    /**
     * Get refund history for a gym.
     *
     * @param gymId The gym ID
     * @return List of refund responses
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory(UUID gymId) {
        List<PaymentRefund> refunds = refundRepository.findByGymIdOrderByCreatedAtDesc(gymId);
        return refunds.stream()
                .map(this::toRefundResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific refund by ID.
     *
     * @param gymId The gym ID (for authorization)
     * @param refundId The refund ID
     * @return RefundResponse
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefund(UUID gymId, UUID refundId) {
        PaymentRefund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new DomainException("REFUND_NOT_FOUND", "Refund not found"));

        // Verify the refund belongs to this gym
        if (!refund.getGymId().equals(gymId)) {
            throw new DomainException("REFUND_ACCESS_DENIED", "This refund does not belong to your gym");
        }

        return toRefundResponse(refund);
    }

    /**
     * Convert PaymentRefund entity to RefundResponse DTO.
     */
    private RefundResponse toRefundResponse(PaymentRefund refund) {
        return RefundResponse.builder()
                .refundId(refund.getStripeRefundId())
                .paymentIntentId(refund.getStripePaymentIntentId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus().name().toLowerCase())
                .reason(refund.getCustomReason() != null ? refund.getCustomReason() : refund.getReason())
                .createdAt(refund.getStripeCreatedAt())
                .build();
    }

    /**
     * Map custom reason string to Stripe's allowed reason values.
     */
    private RefundCreateParams.Reason mapToStripeReason(String reason) {
        if (reason == null) {
            return null;
        }

        String lowerReason = reason.toLowerCase();
        if (lowerReason.contains("duplicate")) {
            return RefundCreateParams.Reason.DUPLICATE;
        } else if (lowerReason.contains("fraud")) {
            return RefundCreateParams.Reason.FRAUDULENT;
        } else if (lowerReason.contains("request") || lowerReason.contains("customer")) {
            return RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER;
        }

        return null; // Will not set a Stripe reason, but custom_reason metadata is preserved
    }

    // Helper methods
    private GymSubscription getSubscription(UUID gymId) {
        return subscriptionRepository.findByGymId(gymId)
                .orElseThrow(() -> new DomainException("SUBSCRIPTION_NOT_FOUND",
                    "No subscription found for this gym"));
    }

    private void validateStripeConfigured() {
        if (!stripeConfig.isConfigured()) {
            throw new DomainException("STRIPE_NOT_CONFIGURED",
                "Payment processing is not configured. Please contact support.");
        }
    }

    private com.gymmate.payment.domain.PaymentMethod savePaymentMethod(UUID gymId, com.stripe.model.PaymentMethod stripePaymentMethod, boolean isDefault) {
        com.stripe.model.PaymentMethod.Card card = stripePaymentMethod.getCard();

        com.gymmate.payment.domain.PaymentMethod method = com.gymmate.payment.domain.PaymentMethod.forGym(gymId, stripePaymentMethod.getId(),
                PaymentMethodType.fromStripeType(stripePaymentMethod.getType()));

        method.setProviderCustomerId(stripePaymentMethod.getCustomer());
        method.setCardBrand(card != null ? card.getBrand() : null);
        method.setCardLastFour(card != null ? card.getLast4() : null);
        method.setCardExpiresMonth(card != null ? card.getExpMonth().intValue() : null);
        method.setCardExpiresYear(card != null ? card.getExpYear().intValue() : null);
        method.setIsDefault(isDefault);

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
                            .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP))
                            .currency(stripeInvoice.getCurrency().toUpperCase())
                            .status(InvoiceStatus.fromStripeStatus(stripeInvoice.getStatus()))
                            .description(stripeInvoice.getDescription())
                            .periodStart(stripeInvoice.getPeriodStart() != null ?
                              utilityService.secondsToLocalDateTime(stripeInvoice.getPeriodStart()) : null)
                            .periodEnd(stripeInvoice.getPeriodEnd() != null ?
                              utilityService.secondsToLocalDateTime(stripeInvoice.getPeriodEnd()) : null)
                            .dueDate(stripeInvoice.getDueDate() != null ?
                              utilityService.secondsToLocalDateTime(stripeInvoice.getDueDate()) : null)
                            .paidAt(stripeInvoice.getStatusTransitions() != null &&
                                    stripeInvoice.getStatusTransitions().getPaidAt() != null ?
                              utilityService.secondsToLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt()) : null)
                            .invoicePdfUrl(stripeInvoice.getInvoicePdf())
                            .hostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl())
                            .build();

                    return invoiceRepository.save(invoice);
                });
    }

    private PaymentMethodResponse toPaymentMethodResponse(com.gymmate.payment.domain.PaymentMethod method) {
        return PaymentMethodResponse.builder()
                .id(method.getId())
                .type(method.getMethodType() != null ? method.getMethodType().name().toLowerCase() : null)
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
}

