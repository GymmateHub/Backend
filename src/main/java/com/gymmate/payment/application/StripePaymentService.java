package com.gymmate.payment.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.payment.api.dto.InvoiceResponse;
import com.gymmate.payment.api.dto.PaymentMethodResponse;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.GymInvoiceRepository;
import com.gymmate.payment.infrastructure.PaymentMethodRepository;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.service.UtilityService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling Stripe platform payments (Organisation → GymMate).
 * Manages customer creation, payment methods, subscriptions, and invoices.
 *
 * IMPORTANT: Subscriptions are at the ORGANISATION level, not gym level.
 * Payment methods and invoices can be gym-specific for Stripe Connect scenarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService {

    private final StripeConfig stripeConfig;
    private final GymRepository gymRepository;
    private final OrganisationRepository organisationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final UtilityService utilityService;

    // ==================== Organisation-level operations ====================

    /**
     * Create or get a Stripe customer for an organisation.
     * The Stripe customer represents the organisation (billing entity).
     */
    @Transactional
    public String createOrGetStripeCustomerForOrganisation(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        // Return existing customer ID if present
        if (subscription.getStripeCustomerId() != null) {
            return subscription.getStripeCustomerId();
        }

        validateStripeConfigured();

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(organisation.getContactEmail())
                    .setName(organisation.getName())
                    .putMetadata("organisation_id", organisationId.toString())
                    .build();

            Customer customer = Customer.create(params);

            // Update subscription with Stripe customer ID
            subscription.setStripeCustomerId(customer.getId());
            subscriptionRepository.save(subscription);

            log.info("Created Stripe customer {} for organisation {}", customer.getId(), organisationId);
            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_CUSTOMER_CREATE_FAILED",
                "Failed to create payment profile: " + e.getMessage());
        }
    }

    /**
     * Create a Stripe subscription for an organisation.
     */
    @Transactional
    public void createStripeSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier, boolean startTrial) {
        String customerId = createOrGetStripeCustomerForOrganisation(organisationId);
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        validateStripeConfigured();

        // Skip if already has Stripe subscription
        if (subscription.getStripeSubscriptionId() != null) {
            log.info("Organisation {} already has Stripe subscription {}", organisationId, subscription.getStripeSubscriptionId());
            return;
        }

        // Check if tier has a Stripe price ID configured
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
                    .putMetadata("organisation_id", organisationId.toString())
                    .putMetadata("tier_name", tier.getName());

            // Add trial if requested
            if (startTrial && tier.getTrialDays() != null && tier.getTrialDays() > 0) {
                paramsBuilder.setTrialPeriodDays(tier.getTrialDays().longValue());
            }

            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription.create(paramsBuilder.build());

            // Update our subscription with Stripe ID
            subscription.setStripeSubscriptionId(stripeSubData.getId());
            subscriptionRepository.save(subscription);

            log.info("Created Stripe subscription {} for organisation {}", stripeSubData.getId(), organisationId);

        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CREATE_FAILED",
                "Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * Cancel a Stripe subscription for an organisation.
     */
    @Transactional
    public void cancelStripeSubscriptionForOrganisation(UUID organisationId, boolean immediate) {
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        if (subscription.getStripeSubscriptionId() == null) {
            log.info("Organisation {} has no Stripe subscription to cancel", organisationId);
            return;
        }

        validateStripeConfigured();

        try {
            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId());

            if (immediate) {
                stripeSubData.cancel();
                log.info("Immediately cancelled Stripe subscription {} for organisation {}",
                    subscription.getStripeSubscriptionId(), organisationId);
            } else {
                stripeSubData.update(SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build());
                log.info("Scheduled cancellation of Stripe subscription {} for organisation {} at period end",
                    subscription.getStripeSubscriptionId(), organisationId);
            }

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CANCEL_FAILED",
                "Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * Get invoices for an organisation.
     */
    public List<InvoiceResponse> getInvoicesForOrganisation(UUID organisationId) {
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        // Return from our database first (invoices are stored per organisation)
        List<GymInvoice> localInvoices = invoiceRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId);
        if (!localInvoices.isEmpty()) {
            return localInvoices.stream()
                    .map(this::toInvoiceResponse)
                    .collect(Collectors.toList());
        }

        // If no local invoices and we have a Stripe customer, fetch from Stripe
        if (subscription.getStripeCustomerId() != null && stripeConfig.isConfigured()) {
            return fetchInvoicesFromStripeForOrganisation(organisationId, subscription.getStripeCustomerId());
        }

        return List.of();
    }

    /**
     * Fetch invoices from Stripe and cache them locally.
     */
    @Transactional
    public List<InvoiceResponse> fetchInvoicesFromStripeForOrganisation(UUID organisationId, String customerId) {
        try {
            InvoiceListParams params = InvoiceListParams.builder()
                    .setCustomer(customerId)
                    .setLimit(100L)
                    .build();

            InvoiceCollection invoices = Invoice.list(params);

            return invoices.getData().stream()
                    .map(invoice -> {
                        GymInvoice localInvoice = saveInvoiceFromStripeForOrganisation(organisationId, invoice);
                        return toInvoiceResponse(localInvoice);
                    })
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("Failed to fetch invoices from Stripe: {}", e.getMessage());
            return List.of();
        }
    }

    // ==================== Gym-level operations (backward compatible) ====================

    /**
     * Create a Stripe customer for a gym (delegates to organisation).
     * @deprecated Use createOrGetStripeCustomerForOrganisation instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional
    public String createOrGetStripeCustomer(UUID gymId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        return createOrGetStripeCustomerForOrganisation(organisationId);
    }

    /**
     * Attach a payment method to an organisation's Stripe customer via gym context.
     */
    @Transactional
    public PaymentMethodResponse attachPaymentMethod(UUID gymId, String stripePaymentMethodId, boolean setAsDefault) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        String customerId = createOrGetStripeCustomerForOrganisation(organisationId);

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

                // Clear existing defaults in our database for this organisation
                paymentMethodRepository.clearDefaultForOrganisation(organisationId);
            }

            // Save payment method to our database
            com.gymmate.payment.domain.PaymentMethod savedMethod = savePaymentMethod(organisationId, gymId, stripePaymentMethod, setAsDefault);

            log.info("Attached payment method {} to organisation {} (via gym {})", stripePaymentMethodId, organisationId, gymId);
            return toPaymentMethodResponse(savedMethod);

        } catch (StripeException e) {
            log.error("Failed to attach payment method for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_ATTACH_FAILED",
                "Failed to attach payment method: " + e.getMessage());
        }
    }

    /**
     * Get all payment methods for an organisation (via gym context).
     */
    public List<PaymentMethodResponse> getPaymentMethods(UUID gymId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        return paymentMethodRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove a payment method.
     */
    @Transactional
    public void removePaymentMethod(UUID gymId, UUID paymentMethodId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        com.gymmate.payment.domain.PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));

        // Verify ownership via organisation
        if (!organisationId.equals(method.getOrganisationId())) {
            throw new DomainException("PAYMENT_METHOD_ACCESS_DENIED", "You don't have access to this payment method");
        }

        validateStripeConfigured();

        try {
            // Detach from Stripe
            com.stripe.model.PaymentMethod stripeMethod = com.stripe.model.PaymentMethod.retrieve(method.getStripePaymentMethodId());
            stripeMethod.detach();

            // Delete from our database
            paymentMethodRepository.delete(method);

            log.info("Removed payment method {} from organisation {}", paymentMethodId, organisationId);

        } catch (StripeException e) {
            log.error("Failed to remove payment method: {}", e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_REMOVE_FAILED",
                "Failed to remove payment method: " + e.getMessage());
        }
    }

    /**
     * Create a Stripe subscription for a gym (delegates to organisation).
     * @deprecated Use createStripeSubscriptionForOrganisation instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional
    public void createStripeSubscription(UUID gymId, SubscriptionTier tier, boolean startTrial) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        createStripeSubscriptionForOrganisation(organisationId, tier, startTrial);
    }

    /**
     * Cancel a Stripe subscription for a gym (delegates to organisation).
     * @deprecated Use cancelStripeSubscriptionForOrganisation instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional
    public void cancelStripeSubscription(UUID gymId, boolean immediate) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        cancelStripeSubscriptionForOrganisation(organisationId, immediate);
    }

    /**
     * Get invoices for a gym (delegates to organisation).
     * @deprecated Use getInvoicesForOrganisation instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public List<InvoiceResponse> getInvoices(UUID gymId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        return getInvoicesForOrganisation(organisationId);
    }

    /**
     * Fetch invoices from Stripe (delegates to organisation).
     * @deprecated Use fetchInvoicesFromStripeForOrganisation instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @Transactional
    public List<InvoiceResponse> fetchInvoicesFromStripe(UUID gymId, String customerId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);
        return fetchInvoicesFromStripeForOrganisation(organisationId, customerId);
    }

    /**
     * Process a refund for a payment.
     */
    @Transactional
    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

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

            // Save refund to database with organisation context
            PaymentRefund paymentRefund = PaymentRefund.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .stripeRefundId(refund.getId())
                    .stripePaymentIntentId(request.getPaymentIntentId())
                    .stripeChargeId(refund.getCharge())
                    .amount(BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100)))
                    .currency(refund.getCurrency().toUpperCase())
                    .status(RefundStatus.valueOf(refund.getStatus().toUpperCase()))
                    .reason(request.getReason())
                    .receiptNumber(refund.getReceiptNumber())
                    .stripeCreatedAt(utilityService.secondsToLocalDateTime(refund.getCreated()))
                    .build();

            paymentRefundRepository.save(paymentRefund);

            log.info("Processed refund {} for organisation {} (gym {}) on payment {}",
                    refund.getId(), organisationId, gymId, request.getPaymentIntentId());

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
            log.error("Failed to process refund for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_REFUND_FAILED",
                    "Failed to process refund: " + e.getMessage());
        }
    }

    /**
     * Get refund history for an organisation (via gym context).
     */
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory(UUID gymId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        return paymentRefundRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId)
                .stream()
                .map(this::toRefundResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific refund.
     */
    @Transactional(readOnly = true)
    public RefundResponse getRefund(UUID gymId, UUID refundId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new DomainException("REFUND_NOT_FOUND",
                        "Refund not found: " + refundId));

        if (!organisationId.equals(refund.getOrganisationId())) {
            throw new DomainException("REFUND_ACCESS_DENIED",
                    "Access denied to refund: " + refundId);
        }

        return toRefundResponse(refund);
    }

    // ==================== Helper methods ====================

    private Organisation getOrganisation(UUID organisationId) {
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND", "Organisation not found: " + organisationId));
    }

    private Gym getGym(UUID gymId) {
        return gymRepository.findById(gymId)
                .orElseThrow(() -> new DomainException("GYM_NOT_FOUND", "Gym not found: " + gymId));
    }

    private UUID getOrganisationIdFromGym(Gym gym) {
        if (gym.getOrganisationId() == null) {
            throw new DomainException("GYM_NO_ORGANISATION", "Gym is not associated with an organisation");
        }
        return gym.getOrganisationId();
    }

    private Subscription getSubscriptionByOrganisationId(UUID organisationId) {
        return subscriptionRepository.findByOrganisationId(organisationId)
                .orElseThrow(() -> new DomainException("SUBSCRIPTION_NOT_FOUND",
                    "No subscription found for organisation: " + organisationId));
    }

    private void validateStripeConfigured() {
        if (!stripeConfig.isConfigured()) {
            throw new DomainException("STRIPE_NOT_CONFIGURED",
                "Payment processing is not configured. Please contact support.");
        }
    }

    private com.gymmate.payment.domain.PaymentMethod savePaymentMethod(UUID organisationId, UUID gymId,
            com.stripe.model.PaymentMethod stripePaymentMethod, boolean isDefault) {
        com.stripe.model.PaymentMethod.Card card = stripePaymentMethod.getCard();

        com.gymmate.payment.domain.PaymentMethod method = com.gymmate.payment.domain.PaymentMethod.builder()
                .ownerType(PaymentMethodOwnerType.ORGANISATION)
                .ownerId(organisationId)
                .organisationId(organisationId)
                .gymId(gymId)
                .provider("stripe")
                .providerPaymentMethodId(stripePaymentMethod.getId())
                .methodType(PaymentMethodType.CARD)
                .cardBrand(card != null ? card.getBrand() : null)
                .cardLastFour(card != null ? card.getLast4() : null)
                .cardExpiresMonth(card != null && card.getExpMonth() != null ? card.getExpMonth().intValue() : null)
                .cardExpiresYear(card != null && card.getExpYear() != null ? card.getExpYear().intValue() : null)
                .isDefault(isDefault)
                .build();

        return paymentMethodRepository.save(method);
    }

    private GymInvoice saveInvoiceFromStripeForOrganisation(UUID organisationId, Invoice stripeInvoice) {
        return invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElseGet(() -> {
                    GymInvoice invoice = GymInvoice.builder()
                            .organisationId(organisationId)
                            .stripeInvoiceId(stripeInvoice.getId())
                            .invoiceNumber(stripeInvoice.getNumber())
                            .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue())
                                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP))
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
