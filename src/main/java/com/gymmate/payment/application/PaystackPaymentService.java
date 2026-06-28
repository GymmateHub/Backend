package com.gymmate.payment.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.payment.api.dto.InvoiceResponse;
import com.gymmate.payment.api.dto.PaymentMethodResponse;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.domain.GymInvoice;
import com.gymmate.payment.domain.PaymentMethod;
import com.gymmate.payment.domain.PaymentRefund;
import com.gymmate.payment.infrastructure.GymInvoiceRepository;
import com.gymmate.payment.infrastructure.PaymentMethodRepository;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.shared.config.PaystackConfig;
import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.constants.PaymentMethodOwnerType;
import com.gymmate.shared.constants.PaymentMethodType;
import com.gymmate.shared.constants.RefundStatus;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.subscription.domain.Subscription;
import com.gymmate.subscription.domain.SubscriptionRepository;
import com.gymmate.subscription.domain.SubscriptionTier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Paystack implementation for provider-agnostic platform billing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackPaymentService implements PaymentGateway {

    private final PaystackConfig paystackConfig;
    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;
    private final GymRepository gymRepository;
    private final OrganisationRepository organisationRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final GymInvoiceRepository invoiceRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    @Override
    public String providerName() {
        return "paystack";
    }

    @Override
    @Transactional
    public String createOrGetCustomerForOrganisation(UUID organisationId) {
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);
        if (subscription.getProviderCustomerId() != null && !subscription.getProviderCustomerId().isBlank()) {
            return subscription.getProviderCustomerId();
        }

        validatePaystackConfigured();
        Organisation organisation = getOrganisation(organisationId);

        Map<String, Object> body = new HashMap<>();
        body.put("email", organisation.getContactEmail());
        body.put("first_name", organisation.getName());
        body.put("metadata", Map.of("organisation_id", organisationId.toString()));

        JsonNode data = paystackPost("/customer", body);
        String customerCode = data.path("customer_code").asText(null);
        if (customerCode == null || customerCode.isBlank()) {
            throw new DomainException("PAYSTACK_CUSTOMER_CREATE_FAILED", "Could not create Paystack customer profile");
        }

        subscription.setProviderCustomerId(customerCode);
        subscriptionRepository.save(subscription);
        return customerCode;
    }

    @Override
    @Transactional
    public void createSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier, boolean startTrial) {
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);
        if (subscription.getProviderSubscriptionId() != null && !subscription.getProviderSubscriptionId().isBlank()) {
            return;
        }

        String customerCode = createOrGetCustomerForOrganisation(organisationId);

        String planCode = tier.getProviderPlanId();
        if (planCode == null || planCode.isBlank()) {
            log.warn("Tier {} has no provider plan code configured; skipping remote subscription creation", tier.getName());
            return;
        }

        String authorizationCode = paymentMethodRepository.findDefaultForOrganisation(organisationId)
                .map(PaymentMethod::getProviderPaymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_REQUIRED",
                        "Attach a default payment method before creating a Paystack subscription"));

        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerCode);
        body.put("plan", planCode);
        body.put("authorization", authorizationCode);
        if (startTrial && tier.getTrialDays() != null && tier.getTrialDays() > 0) {
            body.put("start_date", LocalDateTime.now().plusDays(tier.getTrialDays()).toString());
        }

        JsonNode data = paystackPost("/subscription", body);
        String subscriptionCode = data.path("subscription_code").asText(null);
        if (subscriptionCode != null && !subscriptionCode.isBlank()) {
            subscription.setProviderSubscriptionId(subscriptionCode);
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscriptionForOrganisation(UUID organisationId, boolean immediate) {
        Subscription subscription = getSubscriptionByOrganisationId(organisationId);
        if (subscription.getProviderSubscriptionId() == null || subscription.getProviderSubscriptionId().isBlank()) {
            return;
        }

        if (!immediate) {
            throw new DomainException("PAYSTACK_CANCEL_AT_PERIOD_END_UNSUPPORTED",
                    "Paystack cancel at period end is not supported yet. Use immediate cancellation.");
        }

        throw new DomainException("PAYSTACK_SUBSCRIPTION_CANCEL_MANUAL",
                "Paystack subscription cancellation requires email token flow and is not automated yet.");
    }

    @Override
    public List<InvoiceResponse> getInvoicesForOrganisation(UUID organisationId) {
        return invoiceRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId)
                .stream()
                .map(this::toInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentMethodResponse attachPaymentMethod(UUID organisationId, String providerPaymentMethodId,
            boolean setAsDefault) {
        if (setAsDefault) {
            paymentMethodRepository.clearDefaultForOrganisation(organisationId);
        }

        PaymentMethod method = PaymentMethod.builder()
                .ownerType(PaymentMethodOwnerType.ORGANISATION)
                .ownerId(organisationId)
                .organisationId(organisationId)
                .provider("paystack")
                .providerPaymentMethodId(providerPaymentMethodId)
                .methodType(PaymentMethodType.CARD)
                .isDefault(setAsDefault)
                .build();

        return toPaymentMethodResponse(paymentMethodRepository.save(method));
    }

    @Override
    public List<PaymentMethodResponse> getPaymentMethods(UUID organisationId) {
        return paymentMethodRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::toPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removePaymentMethod(UUID organisationId, UUID paymentMethodId) {
        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new DomainException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found"));

        if (!organisationId.equals(method.getOrganisationId())) {
            throw new DomainException("PAYMENT_METHOD_ACCESS_DENIED", "You don't have access to this payment method");
        }

        paymentMethodRepository.delete(method);
    }

    @Override
    @Transactional
    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        validatePaystackConfigured();

        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        Map<String, Object> body = new HashMap<>();
        body.put("transaction", request.getProviderTransactionId());
        if (request.getAmount() != null) {
            body.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).longValue());
        }
        if (request.getReason() != null && !request.getReason().isBlank()) {
            body.put("customer_note", request.getReason());
            body.put("merchant_note", request.getReason());
        }

        JsonNode data = paystackPost("/refund", body);

        String refundId = data.path("reference").asText();
        if (refundId == null || refundId.isBlank()) {
            refundId = String.valueOf(data.path("id").asLong());
        }

        String status = data.path("status").asText("pending");
        String currency = data.path("currency").asText("NGN").toUpperCase();
        BigDecimal amount = BigDecimal.valueOf(data.path("amount").asLong(0))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        PaymentRefund paymentRefund = PaymentRefund.builder()
                .organisationId(organisationId)
                .gymId(gymId)
                .providerRefundId(refundId)
                .providerTransactionId(request.getProviderTransactionId())
                .amount(amount)
                .currency(currency)
                .status(mapPaystackRefundStatus(status))
                .reason(request.getReason())
                .providerCreatedAt(LocalDateTime.now())
                .build();

        paymentRefundRepository.save(paymentRefund);

        return RefundResponse.builder()
                .refundId(paymentRefund.getProviderRefundId())
                .providerTransactionId(request.getProviderTransactionId())
                .amount(paymentRefund.getAmount())
                .currency(paymentRefund.getCurrency())
                .status(paymentRefund.getStatus().name())
                .reason(paymentRefund.getReason())
                .createdAt(paymentRefund.getProviderCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundHistory(UUID gymId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        return paymentRefundRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId)
                .stream()
                .map(this::toRefundResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefund(UUID gymId, UUID refundId) {
        Gym gym = getGym(gymId);
        UUID organisationId = getOrganisationIdFromGym(gym);

        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new DomainException("REFUND_NOT_FOUND", "Refund not found: " + refundId));

        if (!organisationId.equals(refund.getOrganisationId())) {
            throw new DomainException("REFUND_ACCESS_DENIED", "Access denied to refund: " + refundId);
        }

        return toRefundResponse(refund);
    }

    private JsonNode paystackPost(String path, Map<String, Object> body) {
        try {
            String response = restClientBuilder.baseUrl(paystackConfig.getBaseUrl()).build()
                    .post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + paystackConfig.getSecretKey())
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            if (!root.path("status").asBoolean(false)) {
                throw new DomainException("PAYSTACK_API_ERROR",
                        root.path("message").asText("Paystack request failed"));
            }
            return root.path("data");
        } catch (RestClientException ex) {
            throw new DomainException("PAYSTACK_API_ERROR", "Paystack request failed: " + ex.getMessage());
        } catch (Exception ex) {
            throw new DomainException("PAYSTACK_API_PARSE_ERROR", "Could not parse Paystack response");
        }
    }

    private RefundStatus mapPaystackRefundStatus(String status) {
        return switch (status.toLowerCase()) {
            case "processed", "success", "succeeded" -> RefundStatus.SUCCEEDED;
            case "failed" -> RefundStatus.FAILED;
            default -> RefundStatus.PENDING;
        };
    }

    private void validatePaystackConfigured() {
        if (!paystackConfig.isConfigured()) {
            throw new DomainException("PAYSTACK_NOT_CONFIGURED",
                    "Paystack is not configured. Set PAYSTACK_SECRET_KEY and try again.");
        }
    }

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
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : InvoiceStatus.OPEN.name())
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


