package com.gymmate.membership.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.membership.domain.*;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.membership.infrastructure.MemberPaymentMethodRepository;
import com.gymmate.membership.infrastructure.MembershipPlanRepository;
import com.gymmate.payment.application.StripeConnectService;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.user.domain.Member;
import com.gymmate.user.infrastructure.MemberRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.param.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling member payment operations via Stripe Connect.
 * Manages member payment methods and subscriptions on gym's Stripe accounts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPaymentService {

    private final StripeConfig stripeConfig;
    private final StripeConnectService connectService;
    private final GymRepository gymRepository;
    private final MemberRepository memberRepository;
    private final MemberPaymentMethodRepository paymentMethodRepository;
    private final MemberMembershipRepository membershipRepository;
    private final MemberInvoiceRepository invoiceRepository;
    private final MembershipPlanRepository planRepository;

    /**
     * Create or get a Stripe customer for a member on the gym's Connect account.
     */
    @Transactional
    public String createOrGetMemberCustomer(UUID gymId, UUID memberId, String email, String name) {
        Gym gym = getGym(gymId);
        validateGymCanAcceptPayments(gym);

        // Check if member already has a customer ID for this gym
        MemberMembership membership = membershipRepository.findByMemberIdAndGymIdAndStatusIn(
                memberId, gymId, List.of(MembershipStatus.ACTIVE, MembershipStatus.PAUSED))
                .stream().findFirst().orElse(null);

        if (membership != null && membership.getStripeCustomerId() != null) {
            return membership.getStripeCustomerId();
        }

        try {
            RequestOptions connectOptions = getConnectRequestOptions(gym.getStripeConnectAccountId());

            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(name)
                    .putMetadata("member_id", memberId.toString())
                    .putMetadata("gym_id", gymId.toString())
                    .build();

            Customer customer = Customer.create(params, connectOptions);

            log.info("Created Stripe customer {} for member {} on gym {} Connect account",
                    customer.getId(), memberId, gymId);

            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer for member {} on gym {}: {}",
                    memberId, gymId, e.getMessage());
            throw new DomainException("STRIPE_CUSTOMER_CREATE_FAILED",
                    "Failed to create payment profile: " + e.getMessage());
        }
    }

    /**
     * Attach a payment method to a member on the gym's Connect account.
     */
    @Transactional
    public MemberPaymentMethodResponse attachPaymentMethod(UUID gymId, UUID memberId, String email, String name,
                                                           String stripePaymentMethodId, boolean setAsDefault) {
        Gym gym = getGym(gymId);
        validateGymCanAcceptPayments(gym);

        String customerId = createOrGetMemberCustomer(gymId, memberId, email, name);

        try {
            RequestOptions connectOptions = getConnectRequestOptions(gym.getStripeConnectAccountId());

            // Attach payment method to customer
            PaymentMethod paymentMethod = PaymentMethod.retrieve(stripePaymentMethodId, connectOptions);
            paymentMethod.attach(PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build(), connectOptions);

            // Set as default if requested
            if (setAsDefault) {
                Customer.retrieve(customerId, connectOptions)
                        .update(CustomerUpdateParams.builder()
                                .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                                        .setDefaultPaymentMethod(stripePaymentMethodId)
                                        .build())
                                .build(), connectOptions);

                // Clear existing defaults
                paymentMethodRepository.clearDefaultForMember(memberId, gymId);
            }

            // Save to our database
            MemberPaymentMethod savedMethod = saveMemberPaymentMethod(gymId, memberId, paymentMethod, setAsDefault);

            log.info("Attached payment method {} for member {} on gym {}", stripePaymentMethodId, memberId, gymId);
            return toPaymentMethodResponse(savedMethod);

        } catch (StripeException e) {
            log.error("Failed to attach payment method for member {}: {}", memberId, e.getMessage());
            throw new DomainException("STRIPE_PAYMENT_METHOD_ATTACH_FAILED",
                    "Failed to attach payment method: " + e.getMessage());
        }
    }

    /**
     * Get all payment methods for a member at a specific gym.
     */
    public List<MemberPaymentMethodResponse> getMemberPaymentMethods(UUID gymId, UUID memberId) {
        return paymentMethodRepository.findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(memberId, gymId)
                .stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a subscription for a member on the gym's Connect account.
     */
    @Transactional
    public MemberMembership createMemberSubscription(UUID gymId, UUID memberId, UUID planId,
                                                     String customerId, String paymentMethodId) {
        Gym gym = getGym(gymId);
        validateGymCanAcceptPayments(gym);

        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new DomainException("PLAN_NOT_FOUND", "Membership plan not found"));

        // Validate plan has Stripe price ID
        if (plan.getStripePriceId() == null || plan.getStripePriceId().isBlank()) {
            log.warn("Plan {} does not have Stripe price ID, creating membership without Stripe subscription", planId);
            return createLocalMembership(gymId, memberId, plan, customerId);
        }

        try {
            RequestOptions connectOptions = getConnectRequestOptions(gym.getStripeConnectAccountId());

            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(plan.getStripePriceId())
                            .build())
                    .setDefaultPaymentMethod(paymentMethodId)
                    .putMetadata("gym_id", gymId.toString())
                    .putMetadata("member_id", memberId.toString())
                    .putMetadata("plan_id", planId.toString());

            // Add application fee if configured
            if (stripeConfig.getApplicationFeePercent() != null && stripeConfig.getApplicationFeePercent() > 0) {
                paramsBuilder.setApplicationFeePercent(BigDecimal.valueOf(stripeConfig.getApplicationFeePercent()));
            }

            Subscription subscription = Subscription.create(paramsBuilder.build(), connectOptions);

            // Create local membership record - Stripe SDK v31+ access via raw JSON
            Long periodEnd = null;
            try {
                com.google.gson.JsonObject rawJson = subscription.getRawJsonObject();
                if (rawJson.has("current_period_end") && !rawJson.get("current_period_end").isJsonNull()) {
                    periodEnd = rawJson.get("current_period_end").getAsLong();
                }
            } catch (Exception e) {
                log.warn("Failed to get period end from subscription: {}", e.getMessage());
            }

            final Long finalPeriodEnd = periodEnd;
            MemberMembership membership = MemberMembership.builder()
                    .memberId(memberId)
                    .membershipPlanId(planId)
                    .startDate(LocalDate.now())
                    .endDate(finalPeriodEnd != null ? toLocalDate(finalPeriodEnd) : null)
                    .monthlyAmount(plan.getPrice())
                    .billingCycle(plan.getBillingCycle())
                    .nextBillingDate(finalPeriodEnd != null ? toLocalDate(finalPeriodEnd) : null)
                    .stripeCustomerId(customerId)
                    .stripeSubscriptionId(subscription.getId())
                    .status(MembershipStatus.ACTIVE)
                    .classCreditsRemaining(plan.getClassCredits())
                    .guestPassesRemaining(plan.getGuestPasses())
                    .trainerSessionsRemaining(plan.getTrainerSessions())
                    .build();

            membershipRepository.save(membership);

            log.info("Created membership {} with Stripe subscription {} for member {} at gym {}",
                    membership.getId(), subscription.getId(), memberId, gymId);

            return membership;

        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription for member {} at gym {}: {}",
                    memberId, gymId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CREATE_FAILED",
                    "Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * Cancel a member's subscription.
     */
    @Transactional
    public void cancelMemberSubscription(UUID membershipId, boolean immediate) {
        MemberMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new DomainException("MEMBERSHIP_NOT_FOUND", "Membership not found"));

        if (membership.getStripeSubscriptionId() == null) {
            // No Stripe subscription, just update local record
            membership.cancel();
            membershipRepository.save(membership);
            return;
        }

        // Get gymId from Member since MemberMembership no longer has direct gymId reference
        Member member = memberRepository.findById(membership.getMemberId())
                .orElseThrow(() -> new DomainException("MEMBER_NOT_FOUND", "Member not found"));

        Gym gym = getGym(member.getGymId());
        validateStripeConfigured();

        try {
            RequestOptions connectOptions = getConnectRequestOptions(gym.getStripeConnectAccountId());
            Subscription subscription = Subscription.retrieve(membership.getStripeSubscriptionId(), connectOptions);

            if (immediate) {
                subscription.cancel(SubscriptionCancelParams.builder().build(), connectOptions);
                membership.cancel();
            } else {
                subscription.update(SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build(), connectOptions);
                membership.setAutoRenew(false);
            }

            membershipRepository.save(membership);
            log.info("Cancelled membership {} (immediate: {})", membershipId, immediate);

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription for membership {}: {}", membershipId, e.getMessage());
            throw new DomainException("STRIPE_SUBSCRIPTION_CANCEL_FAILED",
                    "Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * Get invoices for a member's membership.
     */
    public List<MemberInvoiceResponse> getMemberInvoices(UUID gymId, UUID memberId) {
        return invoiceRepository.findByMemberIdAndGymIdOrderByCreatedAtDesc(memberId, gymId)
                .stream()
                .map(this::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private Gym getGym(UUID gymId) {
        return gymRepository.findById(gymId)
                .orElseThrow(() -> new DomainException("GYM_NOT_FOUND", "Gym not found"));
    }

    private void validateGymCanAcceptPayments(Gym gym) {
        if (gym.getStripeConnectAccountId() == null || !Boolean.TRUE.equals(gym.getStripeChargesEnabled())) {
            throw new DomainException("GYM_CANNOT_ACCEPT_PAYMENTS",
                    "This gym cannot accept payments yet. Please contact the gym to complete their payment setup.");
        }
    }

    private void validateStripeConfigured() {
        if (!stripeConfig.isConfigured()) {
            throw new DomainException("STRIPE_NOT_CONFIGURED",
                    "Payment processing is not configured.");
        }
    }

    private RequestOptions getConnectRequestOptions(String stripeAccountId) {
        return RequestOptions.builder()
                .setStripeAccount(stripeAccountId)
                .build();
    }

    private MemberMembership createLocalMembership(UUID gymId, UUID memberId, MembershipPlan plan, String customerId) {
        MemberMembership membership = MemberMembership.builder()
                .memberId(memberId)
                .membershipPlanId(plan.getId())
                .startDate(LocalDate.now())
                .monthlyAmount(plan.getPrice())
                .billingCycle(plan.getBillingCycle())
                .stripeCustomerId(customerId)
                .status(MembershipStatus.ACTIVE)
                .classCreditsRemaining(plan.getClassCredits())
                .guestPassesRemaining(plan.getGuestPasses())
                .trainerSessionsRemaining(plan.getTrainerSessions())
                .build();

        return membershipRepository.save(membership);
    }

    private MemberPaymentMethod saveMemberPaymentMethod(UUID gymId, UUID memberId,
                                                         PaymentMethod paymentMethod, boolean isDefault) {
        PaymentMethod.Card card = paymentMethod.getCard();

        MemberPaymentMethod method = MemberPaymentMethod.builder()
                .memberId(memberId)
                .stripePaymentMethodId(paymentMethod.getId())
                .type(paymentMethod.getType())
                .cardBrand(card != null ? card.getBrand() : null)
                .lastFour(card != null ? card.getLast4() : null)
                .expiryMonth(card != null ? card.getExpMonth().intValue() : null)
                .expiryYear(card != null ? card.getExpYear().intValue() : null)
                .isDefault(isDefault)
                .build();

        return paymentMethodRepository.save(method);
    }

    private LocalDate toLocalDate(Long epochSeconds) {
        return LocalDate.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private MemberPaymentMethodResponse toPaymentMethodResponse(MemberPaymentMethod method) {
        return MemberPaymentMethodResponse.builder()
                .id(method.getId())
                .type(method.getType())
                .cardBrand(method.getCardBrand())
                .lastFour(method.getLastFour())
                .expiryMonth(method.getExpiryMonth())
                .expiryYear(method.getExpiryYear())
                .isDefault(method.getIsDefault())
                .build();
    }

    private MemberInvoiceResponse toInvoiceResponse(MemberInvoice invoice) {
        return MemberInvoiceResponse.builder()
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

    // DTOs
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberPaymentMethodResponse {
        private UUID id;
        private String type;
        private String cardBrand;
        private String lastFour;
        private Integer expiryMonth;
        private Integer expiryYear;
        private Boolean isDefault;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MemberInvoiceResponse {
        private UUID id;
        private String invoiceNumber;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String description;
        private java.time.LocalDateTime periodStart;
        private java.time.LocalDateTime periodEnd;
        private java.time.LocalDateTime dueDate;
        private java.time.LocalDateTime paidAt;
        private String invoicePdfUrl;
        private String hostedInvoiceUrl;
        private java.time.LocalDateTime createdAt;
    }
}

