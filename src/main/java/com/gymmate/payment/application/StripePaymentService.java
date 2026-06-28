package com.gymmate.payment.application;

import com.gymmate.membership.domain.MemberInvoice;
import com.gymmate.membership.domain.MemberInvoiceStatus;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.events.ChargeDisputedEvent;
import com.gymmate.notification.events.ChargeRefundedEvent;
import com.gymmate.notification.events.PaymentFailedEvent;
import com.gymmate.notification.events.PaymentSuccessEvent;
import com.gymmate.notification.events.SubscriptionPausedEvent;
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
import com.gymmate.payment.infrastructure.PaymentWebhookEventRepository;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.constants.PaymentMethodOwnerType;
import com.gymmate.shared.constants.PaymentMethodType;
import com.gymmate.shared.constants.RefundStatus;
import com.gymmate.shared.constants.SubscriptionStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.service.UtilityService;
import com.gymmate.subscription.domain.Subscription;
import com.gymmate.subscription.domain.SubscriptionRepository;
import com.gymmate.subscription.domain.SubscriptionTier;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling Stripe platform payments (Organisation → GymMate).
 * Manages customer creation, payment methods, subscriptions, and invoices.
 *
 * IMPORTANT: Subscriptions are at the ORGANISATION level, not gym level.
 * Payment methods and invoices can be gym-specific for Stripe Connect
 * scenarios.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentService implements PaymentGateway {

    private final StripeConfig stripeConfig;
    private final GymRepository gymRepository;
    private final OrganisationRepository organisationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final UtilityService utilityService;
    private final NotificationService notificationService;
    private final MemberMembershipJpaRepository memberMembershipRepository;
    private final MemberInvoiceRepository memberInvoiceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ==================== Organisation-level operations ====================

    @Override
    public String providerName() {
        return "stripe";
    }

    @Override
    public String createOrGetCustomerForOrganisation(UUID organisationId) {
        return createOrGetStripeCustomerForOrganisation(organisationId);
    }

    @Override
    public void createSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier, boolean startTrial) {
        createStripeSubscriptionForOrganisation(organisationId, tier, startTrial);
    }

    @Override
    public void cancelSubscriptionForOrganisation(UUID organisationId, boolean immediate) {
        cancelStripeSubscriptionForOrganisation(organisationId, immediate);
    }

    /**
     * Create or get a Stripe customer for an organisation.
     * The Stripe customer represents the organisation (billing entity).
     */
    @Transactional
    public String createOrGetStripeCustomerForOrganisation(UUID organisationId) {
        Organisation organisation = getOrganisation(organisationId);
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        // Return existing customer ID if present
        if (subscription.getProviderCustomerId() != null) {
            return subscription.getProviderCustomerId();
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
            subscription.setProviderCustomerId(customer.getId());
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
    public void createStripeSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier,
            boolean startTrial) {
        String customerId = createOrGetStripeCustomerForOrganisation(organisationId);
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);

        validateStripeConfigured();

        // Skip if already has Stripe subscription
        if (subscription.getProviderSubscriptionId() != null) {
            log.info("Organisation {} already has Stripe subscription {}", organisationId,
                    subscription.getProviderSubscriptionId());
            return;
        }

        // Check if tier has a Stripe price ID configured
        if (tier.getProviderPlanId() == null || tier.getProviderPlanId().isBlank()) {
            log.warn("Tier {} does not have a Stripe price ID configured. Subscription created without Stripe billing.",
                    tier.getName());
            return;
        }

        try {
            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(tier.getProviderPlanId())
                            .build())
                    .putMetadata("organisation_id", organisationId.toString())
                    .putMetadata("tier_name", tier.getName());

            // Add trial if requested
            if (startTrial && tier.getTrialDays() != null && tier.getTrialDays() > 0) {
                paramsBuilder.setTrialPeriodDays(tier.getTrialDays().longValue());
            }

            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription.create(paramsBuilder.build());

            // Update our subscription with Stripe ID
            subscription.setProviderSubscriptionId(stripeSubData.getId());
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

        if (subscription.getProviderSubscriptionId() == null) {
            log.info("Organisation {} has no Stripe subscription to cancel", organisationId);
            return;
        }

        validateStripeConfigured();

        try {
            com.stripe.model.Subscription stripeSubData = com.stripe.model.Subscription
                    .retrieve(subscription.getProviderSubscriptionId());

            if (immediate) {
                stripeSubData.cancel();
                log.info("Immediately cancelled Stripe subscription {} for organisation {}",
                        subscription.getProviderSubscriptionId(), organisationId);
            } else {
                stripeSubData.update(SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build());
                log.info("Scheduled cancellation of Stripe subscription {} for organisation {} at period end",
                        subscription.getProviderSubscriptionId(), organisationId);
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
        if (subscription.getProviderCustomerId() != null && stripeConfig.isConfigured()) {
            return fetchInvoicesFromStripeForOrganisation(organisationId, subscription.getProviderCustomerId());
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

    // ==================== Organisation-level payment method operations
    // ====================

    /**
     * Attach a payment method to an organisation's Stripe customer.
     */
    @Transactional
    public PaymentMethodResponse attachPaymentMethod(UUID organisationId, String providerPaymentMethodId,
            boolean setAsDefault) {
        String customerId = createOrGetStripeCustomerForOrganisation(organisationId);

        validateStripeConfigured();

        try {
            // Attach payment method to customer
            com.stripe.model.PaymentMethod stripePaymentMethod = com.stripe.model.PaymentMethod
                    .retrieve(providerPaymentMethodId);
            stripePaymentMethod.attach(PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build());

            // Set as default if requested
            if (setAsDefault) {
                Customer customer = Customer.retrieve(customerId);
                customer.update(CustomerUpdateParams.builder()
                        .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(providerPaymentMethodId)
                                .build())
                        .build());

                // Clear existing defaults in our database for this organisation
                paymentMethodRepository.clearDefaultForOrganisation(organisationId);
            }

            // Save payment method to our database
            com.gymmate.payment.domain.PaymentMethod savedMethod = savePaymentMethod(organisationId, null,
                    stripePaymentMethod, setAsDefault);

            log.info("Attached payment method {} to organisation {}", providerPaymentMethodId, organisationId);
            return toPaymentMethodResponse(savedMethod);

        } catch (StripeException e) {
            log.error("Failed to attach payment method for organisation {}: {}", organisationId, e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_ATTACH_FAILED",
                    "Failed to attach payment method: " + e.getMessage());
        }
    }

    /**
     * Get all payment methods for an organisation.
     */
    public List<PaymentMethodResponse> getPaymentMethods(UUID organisationId) {
        return paymentMethodRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    /**
     * Remove a payment method from an organisation.
     */
    @Transactional
    public void removePaymentMethod(UUID organisationId, UUID paymentMethodId) {
        com.gymmate.payment.domain.PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));

        // Verify ownership via organisation
        if (!organisationId.equals(method.getOrganisationId())) {
            throw new DomainException("PAYMENT_METHOD_ACCESS_DENIED", "You don't have access to this payment method");
        }

        validateStripeConfigured();

        try {
            // Detach from Stripe
            com.stripe.model.PaymentMethod stripeMethod = com.stripe.model.PaymentMethod
                    .retrieve(method.getProviderPaymentMethodId());
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
     * Process a refund for a payment.
     */
    @Transactional
    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        validateStripeConfigured();

        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(request.getProviderTransactionId());

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
                    .providerRefundId(refund.getId())
                    .providerTransactionId(request.getProviderTransactionId())
                    .providerChargeId(refund.getCharge())
                    .amount(BigDecimal.valueOf(refund.getAmount()).divide(BigDecimal.valueOf(100)))
                    .currency(refund.getCurrency().toUpperCase())
                    .status(RefundStatus.valueOf(refund.getStatus().toUpperCase()))
                    .reason(request.getReason())
                    .receiptNumber(refund.getReceiptNumber())
                    .providerCreatedAt(utilityService.secondsToLocalDateTime(refund.getCreated()))
                    .build();

            paymentRefundRepository.save(paymentRefund);

            log.info("Processed refund {} for organisation {} (gym {}) on payment {}",
                    refund.getId(), organisationId, gymId, request.getProviderTransactionId());

            return RefundResponse.builder()
                    .refundId(refund.getId())
                    .providerTransactionId(request.getProviderTransactionId())
                    .amount(paymentRefund.getAmount())
                    .currency(paymentRefund.getCurrency())
                    .status(paymentRefund.getStatus().name())
                    .reason(request.getReason())
                    .createdAt(paymentRefund.getProviderCreatedAt())
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

    // ==================== Stripe Connect operations ====================

    @Transactional
    public com.gymmate.payment.api.dto.ConnectOnboardingResponse startOnboarding(UUID gymId) {
        Gym gym = getGym(gymId);
        validateStripeConfigured();

        try {
            String accountId;

            if (gym.getProviderConnectAccountId() != null) {
                accountId = gym.getProviderConnectAccountId();
                log.info("Gym {} already has Connect account {}, creating refresh link", gymId, accountId);
            } else {
                AccountCreateParams params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setEmail(gym.getContactEmail())
                        .setBusinessType(AccountCreateParams.BusinessType.COMPANY)
                        .setCompany(AccountCreateParams.Company.builder().setName(gym.getName()).build())
                        .putMetadata("gym_id", gymId.toString())
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                        .setRequested(true).build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                        .setRequested(true).build())
                                .build())
                        .build();

                Account account = Account.create(params);
                accountId = account.getId();
                gym.setProviderConnectAccountId(accountId);
                gymRepository.save(gym);
            }

            String onboardingUrl = createOnboardingLink(accountId, gymId);
            return com.gymmate.payment.api.dto.ConnectOnboardingResponse.builder()
                    .accountId(accountId)
                    .onboardingUrl(onboardingUrl)
                    .build();
        } catch (StripeException e) {
            throw new DomainException("STRIPE_CONNECT_ONBOARDING_FAILED",
                    "Failed to start payment setup: " + e.getMessage());
        }
    }

    public com.gymmate.payment.api.dto.ConnectAccountStatusResponse getAccountStatus(UUID gymId) {
        Gym gym = getGym(gymId);
        if (gym.getProviderConnectAccountId() == null) {
            return com.gymmate.payment.api.dto.ConnectAccountStatusResponse.builder()
                    .chargesEnabled(false)
                    .payoutsEnabled(false)
                    .detailsSubmitted(false)
                    .requiresAction(true)
                    .build();
        }

        validateStripeConfigured();
        try {
            Account account = Account.retrieve(gym.getProviderConnectAccountId());
            updateGymConnectStatus(gym, account);

            LocalDateTime deadline = null;
            if (account.getRequirements() != null && account.getRequirements().getCurrentDeadline() != null) {
                deadline = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(account.getRequirements().getCurrentDeadline()),
                        ZoneId.systemDefault());
            }

            boolean requiresAction = account.getRequirements() != null
                    && account.getRequirements().getCurrentlyDue() != null
                    && !account.getRequirements().getCurrentlyDue().isEmpty();

            return com.gymmate.payment.api.dto.ConnectAccountStatusResponse.builder()
                    .accountId(account.getId())
                    .chargesEnabled(account.getChargesEnabled())
                    .payoutsEnabled(account.getPayoutsEnabled())
                    .detailsSubmitted(account.getDetailsSubmitted())
                    .requiresAction(requiresAction)
                    .currentDeadline(deadline)
                    .build();
        } catch (StripeException e) {
            throw new DomainException("STRIPE_CONNECT_STATUS_FAILED", "Failed to get payment status: " + e.getMessage());
        }
    }

    @Transactional
    public com.gymmate.payment.api.dto.ConnectOnboardingResponse refreshOnboardingLink(UUID gymId) {
        Gym gym = getGym(gymId);
        if (gym.getProviderConnectAccountId() == null) {
            return startOnboarding(gymId);
        }

        validateStripeConfigured();
        try {
            String onboardingUrl = createOnboardingLink(gym.getProviderConnectAccountId(), gymId);
            return com.gymmate.payment.api.dto.ConnectOnboardingResponse.builder()
                    .accountId(gym.getProviderConnectAccountId())
                    .onboardingUrl(onboardingUrl)
                    .build();
        } catch (StripeException e) {
            throw new DomainException("STRIPE_CONNECT_REFRESH_FAILED",
                    "Failed to refresh payment setup link: " + e.getMessage());
        }
    }

    public String getDashboardLink(UUID gymId) {
        Gym gym = getGym(gymId);
        if (gym.getProviderConnectAccountId() == null) {
            throw new DomainException("STRIPE_CONNECT_NOT_SETUP",
                    "Payment account not set up. Please complete onboarding first.");
        }

        validateStripeConfigured();
        try {
            LoginLink loginLink = LoginLink.createOnAccount(gym.getProviderConnectAccountId());
            return loginLink.getUrl();
        } catch (StripeException e) {
            throw new DomainException("STRIPE_DASHBOARD_LINK_FAILED",
                    "Failed to access payment dashboard: " + e.getMessage());
        }
    }

    public boolean canAcceptPayments(UUID gymId) {
        Gym gym = getGym(gymId);
        if (gym.getProviderConnectAccountId() == null) {
            return false;
        }
        if (Boolean.TRUE.equals(gym.getProviderChargesEnabled())) {
            return true;
        }

        try {
            validateStripeConfigured();
            Account account = Account.retrieve(gym.getProviderConnectAccountId());
            updateGymConnectStatus(gym, account);
            return account.getChargesEnabled();
        } catch (StripeException e) {
            log.warn("Failed to check Connect account status for gym {}: {}", gymId, e.getMessage());
            return false;
        }
    }

    public String createOnboardingLink(String accountId, UUID gymId) throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(frontendUrl + "/gym/settings/payments?refresh=true&gymId=" + gymId)
                .setReturnUrl(frontendUrl + "/gym/settings/payments?success=true&gymId=" + gymId)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
        return AccountLink.create(params).getUrl();
    }

    @Transactional
    public void handleAccountDeauthorized(UUID gymId) {
        Gym gym = getGym(gymId);
        gym.setProviderConnectAccountId(null);
        gym.setProviderChargesEnabled(false);
        gym.setProviderPayoutsEnabled(false);
        gym.setProviderDetailsSubmitted(false);
        gym.setProviderOnboardingCompletedAt(null);
        gymRepository.save(gym);
        log.warn("Cleared Stripe Connect fields for deauthorized gym {}", gymId);
    }

    @Transactional
    public void handleAccountUpdated(String accountId) {
        try {
            Account account = Account.retrieve(accountId);
            String gymIdStr = account.getMetadata().get("gym_id");
            if (gymIdStr == null) {
                return;
            }

            UUID gymId = UUID.fromString(gymIdStr);
            Gym gym = getGym(gymId);
            updateGymConnectStatus(gym, account);
        } catch (StripeException e) {
            log.error("Failed to handle account.updated for {}: {}", accountId, e.getMessage());
        }
    }

    @Transactional
    protected void updateGymConnectStatus(Gym gym, Account account) {
        gym.setProviderChargesEnabled(account.getChargesEnabled());
        gym.setProviderPayoutsEnabled(account.getPayoutsEnabled());
        gym.setProviderDetailsSubmitted(account.getDetailsSubmitted());
        if (account.getChargesEnabled() && gym.getProviderOnboardingCompletedAt() == null) {
            gym.setProviderOnboardingCompletedAt(LocalDateTime.now());
        }
        gymRepository.save(gym);
    }

    // ==================== Stripe webhook operations ====================

    @Transactional
    public void processPlatformWebhook(String payload, String signature) {
        Event event = verifyAndParseEvent(payload, signature, stripeConfig.getWebhookSecret());
        if (webhookEventRepository.existsByProviderAndProviderEventId("stripe", event.getId())) {
            return;
        }

        PaymentWebhookEvent webhookEvent = PaymentWebhookEvent.builder()
                .provider("stripe")
                .providerEventId(event.getId())
                .eventType(event.getType())
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);

        try {
            handlePlatformEvent(event);
            webhookEvent.markProcessed();
        } catch (Exception e) {
            webhookEvent.markFailed(e.getMessage());
        }

        webhookEventRepository.save(webhookEvent);
    }

    @Transactional
    public void processConnectWebhook(String payload, String signature) {
        Event event = verifyAndParseEvent(payload, signature, stripeConfig.getConnectWebhookSecret());
        if (webhookEventRepository.existsByProviderAndProviderEventId("stripe", event.getId())) {
            return;
        }

        PaymentWebhookEvent webhookEvent = PaymentWebhookEvent.builder()
                .provider("stripe")
                .providerEventId(event.getId())
                .eventType(event.getType())
                .payload(payload)
                .build();
        webhookEventRepository.save(webhookEvent);

        try {
            handleConnectEvent(event);
            webhookEvent.markProcessed();
        } catch (Exception e) {
            webhookEvent.markFailed(e.getMessage());
        }

        webhookEventRepository.save(webhookEvent);
    }

    private void handlePlatformEvent(Event event) {
        switch (event.getType()) {
            case "customer.subscription.created", "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "customer.subscription.trial_will_end" -> handleTrialWillEnd(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "invoice.created", "invoice.updated" -> handleInvoiceUpdated(event);
            case "customer.subscription.paused" -> handleSubscriptionPaused(event);
            case "charge.disputed" -> handleChargeDisputed(event);
            case "charge.refunded" -> handleChargeRefunded(event);
            default -> log.debug("Unhandled platform event type: {}", event.getType());
        }
    }

    private void handleConnectEvent(Event event) {
        switch (event.getType()) {
            case "account.updated" -> handleAccountUpdatedEvent(event);
            case "account.application.deauthorized" -> handleAccountDeauthorizedEvent(event);
            case "payment_intent.succeeded" -> handleConnectPaymentSucceeded(event);
            case "payment_intent.payment_failed" -> handleConnectPaymentFailed(event);
            case "charge.disputed" -> handleChargeDisputed(event);
            case "charge.refunded" -> handleChargeRefunded(event);
            default -> log.debug("Unhandled Connect event type: {}", event.getType());
        }
    }

    private void handleSubscriptionUpdated(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event, com.stripe.model.Subscription.class);
        if (stripeSubscription == null) {
            return;
        }

        subscriptionRepository.findByProviderSubscriptionId(stripeSubscription.getId()).ifPresent(subscription -> {
            subscription.setStatus(mapStripeStatus(stripeSubscription.getStatus()));
            try {
                com.google.gson.JsonObject rawJson = stripeSubscription.getRawJsonObject();
                if (rawJson.has("current_period_start") && !rawJson.get("current_period_start").isJsonNull()) {
                    subscription.setCurrentPeriodStart(
                            utilityService.secondsToLocalDateTime(rawJson.get("current_period_start").getAsLong()));
                }
                if (rawJson.has("current_period_end") && !rawJson.get("current_period_end").isJsonNull()) {
                    subscription.setCurrentPeriodEnd(
                            utilityService.secondsToLocalDateTime(rawJson.get("current_period_end").getAsLong()));
                }
                if (rawJson.has("trial_start") && !rawJson.get("trial_start").isJsonNull()) {
                    subscription
                            .setTrialStart(utilityService.secondsToLocalDateTime(rawJson.get("trial_start").getAsLong()));
                }
                if (rawJson.has("trial_end") && !rawJson.get("trial_end").isJsonNull()) {
                    subscription.setTrialEnd(utilityService.secondsToLocalDateTime(rawJson.get("trial_end").getAsLong()));
                }
                if (rawJson.has("cancel_at_period_end")) {
                    subscription.setCancelAtPeriodEnd(rawJson.get("cancel_at_period_end").getAsBoolean());
                }
            } catch (Exception ignored) {
            }
            subscriptionRepository.save(subscription);
        });
    }

    private void handleSubscriptionDeleted(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event, com.stripe.model.Subscription.class);
        if (stripeSubscription == null) {
            return;
        }

        subscriptionRepository.findByProviderSubscriptionId(stripeSubscription.getId()).ifPresent(subscription -> {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);
        });
    }

    private void handleTrialWillEnd(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event, com.stripe.model.Subscription.class);
        if (stripeSubscription == null) {
            return;
        }

        subscriptionRepository.findByProviderSubscriptionId(stripeSubscription.getId()).ifPresent(subscription -> {
            if (subscription.getTrialEnd() == null) {
                return;
            }
            long daysUntilExpiry = java.time.Duration.between(LocalDateTime.now(), subscription.getTrialEnd()).toDays();
            com.gymmate.notification.events.SubscriptionExpiringEvent expiringEvent = com.gymmate.notification.events.SubscriptionExpiringEvent
                    .builder()
                    .organisationId(subscription.getOrganisationId())
                    .subscriptionId(subscription.getId())
                    .tierName(subscription.getTier().getDisplayName())
                    .price(subscription.getTier().getPrice())
                    .expiresAt(subscription.getTrialEnd())
                    .daysUntilExpiry((int) daysUntilExpiry)
                    .build();
            eventPublisher.publishEvent(expiringEvent);
        });
    }

    private void handleInvoicePaid(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null) {
            return;
        }

        GymInvoice invoice = invoiceRepository.findByProviderInvoiceId(stripeInvoice.getId())
                .orElseGet(() -> createInvoiceFromStripe(stripeInvoice));
        invoice.markPaid(stripeInvoice.getStatusTransitions() != null && stripeInvoice.getStatusTransitions().getPaidAt() != null
                ? utilityService.secondsToLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt())
                : LocalDateTime.now());
        invoiceRepository.save(invoice);

        if (invoice.getOrganisationId() != null) {
            PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder()
                    .organisationId(invoice.getOrganisationId())
                    .gymId(invoice.getOrganisationId())
                    .amount(invoice.getAmount())
                    .invoiceNumber(invoice.getInvoiceNumber())
                    .invoiceUrl(invoice.getHostedInvoiceUrl())
                    .periodEnd(invoice.getPeriodEnd())
                    .build();
            eventPublisher.publishEvent(successEvent);
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null) {
            return;
        }

        invoiceRepository.findByProviderInvoiceId(stripeInvoice.getId()).ifPresent(invoice -> {
            invoice.markFailed();
            invoiceRepository.save(invoice);
        });

        try {
            com.google.gson.JsonObject rawJson = stripeInvoice.getRawJsonObject();
            if (!rawJson.has("subscription") || rawJson.get("subscription").isJsonNull()) {
                return;
            }
            String subscriptionId = rawJson.get("subscription").getAsString();
            subscriptionRepository.findByProviderSubscriptionId(subscriptionId).ifPresent(subscription -> {
                subscription.markPastDue();
                subscriptionRepository.save(subscription);
                LocalDateTime nextRetryDate = LocalDateTime.now().plusDays(3);
                if (rawJson.has("next_payment_attempt") && !rawJson.get("next_payment_attempt").isJsonNull()) {
                    nextRetryDate = utilityService.secondsToLocalDateTime(rawJson.get("next_payment_attempt").getAsLong());
                }
                BigDecimal amount = BigDecimal.valueOf(stripeInvoice.getAmountDue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                        .organisationId(subscription.getOrganisationId())
                        .gymId(subscription.getOrganisationId())
                        .amount(amount)
                        .failureReason("Payment could not be processed")
                        .nextRetryDate(nextRetryDate)
                        .invoiceId(stripeInvoice.getId())
                        .build();
                eventPublisher.publishEvent(paymentFailedEvent);
            });
        } catch (Exception ignored) {
        }
    }

    private void handleInvoiceUpdated(Event event) {
        Invoice stripeInvoice = extractEventObject(event, Invoice.class);
        if (stripeInvoice == null || stripeInvoice.getCustomer() == null) {
            return;
        }

        subscriptionRepository.findByProviderCustomerId(stripeInvoice.getCustomer()).ifPresent(subscription -> {
            GymInvoice invoice = invoiceRepository.findByProviderInvoiceId(stripeInvoice.getId())
                    .orElseGet(() -> createInvoiceFromStripe(stripeInvoice, null));
            invoice.setStatus(InvoiceStatus.fromProviderStatus(stripeInvoice.getStatus()));
            invoice.setInvoicePdfUrl(stripeInvoice.getInvoicePdf());
            invoice.setHostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl());
            invoiceRepository.save(invoice);
        });
    }

    private void handleAccountUpdatedEvent(Event event) {
        Account account = extractEventObject(event, Account.class);
        if (account != null) {
            handleAccountUpdated(account.getId());
        }
    }

    private void handleAccountDeauthorizedEvent(Event event) {
        Account account = extractEventObject(event, Account.class);
        if (account == null) {
            return;
        }
        String gymIdStr = account.getMetadata().get("gym_id");
        if (gymIdStr == null) {
            return;
        }

        UUID gymId = UUID.fromString(gymIdStr);
        handleAccountDeauthorized(gymId);

        String orgIdStr = account.getMetadata().get("organisation_id");
        UUID organisationId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;
        if (organisationId != null) {
            notificationService.createAndBroadcast(
                    "⚠️ Stripe Account Disconnected",
                    "Your Stripe Connect account has been disconnected. Member payments will not be processed until you reconnect.",
                    organisationId,
                    NotificationPriority.CRITICAL,
                    "STRIPE_CONNECT_DEAUTHORIZED",
                    Map.of("gymId", gymId.toString()));
        }
    }

    private void handleConnectPaymentSucceeded(Event event) {
        PaymentIntent paymentIntent = extractEventObject(event, PaymentIntent.class);
        if (paymentIntent == null) {
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = paymentIntent.getCurrency() != null ? paymentIntent.getCurrency().toUpperCase() : "USD";

        var metadata = paymentIntent.getMetadata();
        String membershipId = metadata != null ? metadata.get("membership_id") : null;
        String gymIdStr = metadata != null ? metadata.get("gym_id") : null;

        if (membershipId != null) {
            memberMembershipRepository.findById(UUID.fromString(membershipId)).ifPresent(membership -> {
                if (membership.getStatus() == MembershipStatus.EXPIRED || membership.getStatus() == MembershipStatus.CANCELLED) {
                    membership.setStatus(MembershipStatus.ACTIVE);
                    memberMembershipRepository.save(membership);
                }

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

        if (gymIdStr != null && membershipId != null) {
            UUID gymId = UUID.fromString(gymIdStr);
            UUID organisationId = memberMembershipRepository.findById(UUID.fromString(membershipId))
                    .map(com.gymmate.membership.domain.MemberMembership::getOrganisationId).orElse(null);

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
        if (paymentIntent == null) {
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String failureMessage = paymentIntent.getLastPaymentError() != null
                ? paymentIntent.getLastPaymentError().getMessage()
                : "Payment could not be processed";

        var metadata = paymentIntent.getMetadata();
        String membershipId = metadata != null ? metadata.get("membership_id") : null;
        String gymIdStr = metadata != null ? metadata.get("gym_id") : null;

        UUID organisationId = null;
        if (membershipId != null) {
            var membershipOpt = memberMembershipRepository.findById(UUID.fromString(membershipId));
            if (membershipOpt.isPresent()) {
                var membership = membershipOpt.get();
                organisationId = membership.getOrganisationId();
                membership.setStatus(MembershipStatus.PAST_DUE);
                memberMembershipRepository.save(membership);

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

        if (gymIdStr != null && organisationId != null) {
            PaymentFailedEvent failedEvent = PaymentFailedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(UUID.fromString(gymIdStr))
                    .amount(amount)
                    .failureReason(failureMessage)
                    .nextRetryDate(LocalDateTime.now().plusDays(3))
                    .invoiceId(paymentIntent.getId())
                    .build();
            eventPublisher.publishEvent(failedEvent);
        }
    }

    private void handleSubscriptionPaused(Event event) {
        com.stripe.model.Subscription stripeSubscription = extractEventObject(event, com.stripe.model.Subscription.class);
        if (stripeSubscription == null) {
            return;
        }

        subscriptionRepository.findByProviderSubscriptionId(stripeSubscription.getId()).ifPresent(subscription -> {
            subscription.setStatus(SubscriptionStatus.PAUSED);
            subscriptionRepository.save(subscription);

            SubscriptionPausedEvent pausedEvent = SubscriptionPausedEvent.builder()
                    .organisationId(subscription.getOrganisationId())
                    .subscriptionId(subscription.getId())
                    .tierName(subscription.getTier() != null ? subscription.getTier().getDisplayName() : "Unknown")
                    .pausedAt(LocalDateTime.now())
                    .build();
            eventPublisher.publishEvent(pausedEvent);
        });
    }

    private void handleChargeDisputed(Event event) {
        Dispute dispute = extractEventObject(event, Dispute.class);
        if (dispute == null) {
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(dispute.getAmount())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = dispute.getCurrency() != null ? dispute.getCurrency().toUpperCase() : "USD";
        String reason = dispute.getReason() != null ? dispute.getReason() : "not_provided";

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
        }
    }

    private void handleChargeRefunded(Event event) {
        Charge charge = extractEventObject(event, Charge.class);
        if (charge == null) {
            return;
        }

        BigDecimal amountRefunded = BigDecimal.valueOf(charge.getAmountRefunded())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        String currency = charge.getCurrency() != null ? charge.getCurrency().toUpperCase() : "USD";
        UUID organisationId = resolveOrganisationFromCharge(charge);

        if (Boolean.TRUE.equals(charge.getRefunded())) {
            try {
                com.google.gson.JsonObject raw = charge.getRawJsonObject();
                if (raw != null && raw.has("invoice") && !raw.get("invoice").isJsonNull()) {
                    String invoiceId = raw.get("invoice").getAsString();
                    invoiceRepository.findByProviderInvoiceId(invoiceId).ifPresent(invoice -> {
                        invoice.setStatus(InvoiceStatus.REFUNDED);
                        invoiceRepository.save(invoice);
                    });
                }
            } catch (Exception ignored) {
            }
        }

        if (organisationId != null) {
            String refundReason = "Refund processed";
            String latestRefundId = null;
            if (charge.getRefunds() != null && charge.getRefunds().getData() != null && !charge.getRefunds().getData().isEmpty()) {
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
        }
    }

    private UUID resolveOrganisationFromDispute(Dispute dispute) {
        if (dispute.getMetadata() != null) {
            String orgIdStr = dispute.getMetadata().get("organisation_id");
            if (orgIdStr != null) {
                return UUID.fromString(orgIdStr);
            }
        }

        try {
            com.google.gson.JsonObject raw = dispute.getRawJsonObject();
            if (raw != null && raw.has("charge") && !raw.get("charge").isJsonNull()) {
                String customer = raw.has("customer") && !raw.get("customer").isJsonNull()
                        ? raw.get("customer").getAsString()
                        : null;
                if (customer != null) {
                    return subscriptionRepository.findByProviderCustomerId(customer)
                            .map(com.gymmate.subscription.domain.Subscription::getOrganisationId)
                            .orElse(null);
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private UUID resolveOrganisationFromCharge(Charge charge) {
        if (charge.getMetadata() != null) {
            String orgIdStr = charge.getMetadata().get("organisation_id");
            if (orgIdStr != null) {
                return UUID.fromString(orgIdStr);
            }
        }

        if (charge.getCustomer() != null) {
            UUID fromCustomer = subscriptionRepository.findByProviderCustomerId(charge.getCustomer())
                    .map(com.gymmate.subscription.domain.Subscription::getOrganisationId)
                    .orElse(null);
            if (fromCustomer != null) {
                return fromCustomer;
            }
        }

        try {
            com.google.gson.JsonObject raw = charge.getRawJsonObject();
            if (raw != null && raw.has("invoice") && !raw.get("invoice").isJsonNull()) {
                String invoiceId = raw.get("invoice").getAsString();
                return invoiceRepository.findByProviderInvoiceId(invoiceId)
                        .map(GymInvoice::getOrganisationId)
                        .orElse(null);
            }
        } catch (Exception ignored) {
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
            throw new DomainException("INVALID_WEBHOOK_SIGNATURE", "Invalid webhook signature");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T extractEventObject(Event event, @SuppressWarnings("unused") Class<T> clazz) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        if (deserializer.getObject().isPresent()) {
            return (T) deserializer.getObject().get();
        }
        return null;
    }

    private GymInvoice createInvoiceFromStripe(Invoice stripeInvoice) {
        String orgIdStr = stripeInvoice.getMetadata().get("organisation_id");
        UUID organisationId = orgIdStr != null ? UUID.fromString(orgIdStr) : null;
        return createInvoiceFromStripe(stripeInvoice, organisationId);
    }

    private GymInvoice createInvoiceFromStripe(Invoice stripeInvoice, UUID organisationId) {
        if (organisationId == null) {
            var subscription = subscriptionRepository.findByProviderCustomerId(stripeInvoice.getCustomer());
            if (subscription.isPresent()) {
                organisationId = subscription.get().getOrganisationId();
            }
        }

        return GymInvoice.builder()
                .organisationId(organisationId)
                .providerInvoiceId(stripeInvoice.getId())
                .invoiceNumber(stripeInvoice.getNumber())
                .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue()).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP))
                .currency(stripeInvoice.getCurrency() != null ? stripeInvoice.getCurrency().toUpperCase() : "USD")
                .status(InvoiceStatus.fromProviderStatus(stripeInvoice.getStatus()))
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

    // ==================== Helper methods ====================

    private Organisation getOrganisation(UUID organisationId) {
        return organisationRepository.findById(organisationId)
                .orElseThrow(() -> new DomainException("ORGANISATION_NOT_FOUND",
                        "Organisation not found: " + organisationId));
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
        return invoiceRepository.findByProviderInvoiceId(stripeInvoice.getId())
                .orElseGet(() -> {
                    GymInvoice invoice = GymInvoice.builder()
                            .organisationId(organisationId)
                            .providerInvoiceId(stripeInvoice.getId())
                            .invoiceNumber(stripeInvoice.getNumber())
                            .amount(BigDecimal.valueOf(stripeInvoice.getAmountDue())
                                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP))
                            .currency(stripeInvoice.getCurrency().toUpperCase())
                            .status(InvoiceStatus.fromProviderStatus(stripeInvoice.getStatus()))
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
                            .paidAt(stripeInvoice.getStatusTransitions() != null &&
                                    stripeInvoice.getStatusTransitions().getPaidAt() != null
                                            ? utilityService.secondsToLocalDateTime(
                                                    stripeInvoice.getStatusTransitions().getPaidAt())
                                            : null)
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
                .refundId(refund.getProviderRefundId())
                .providerTransactionId(refund.getProviderTransactionId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .status(refund.getStatus().name())
                .reason(refund.getReason())
                .createdAt(refund.getProviderCreatedAt())
                .build();
    }
}
