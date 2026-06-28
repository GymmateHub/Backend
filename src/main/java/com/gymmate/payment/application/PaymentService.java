package com.gymmate.payment.application;

import com.gymmate.payment.api.dto.InvoiceResponse;
import com.gymmate.payment.api.dto.PaymentMethodResponse;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.subscription.domain.SubscriptionTier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Routes payment operations to the configured provider implementation.
 */
@Service
public class PaymentService {

    private final PaymentProviderProperties paymentProviderProperties;
    private final PaymentGateway stripeGateway;
    private final PaymentGateway paystackGateway;

    public PaymentService(
            PaymentProviderProperties paymentProviderProperties,
            @Qualifier("stripePaymentService") PaymentGateway stripeGateway,
            @Qualifier("paystackPaymentService") PaymentGateway paystackGateway) {
        this.paymentProviderProperties = paymentProviderProperties;
        this.stripeGateway = stripeGateway;
        this.paystackGateway = paystackGateway;
    }

    public String createOrGetCustomerForOrganisation(UUID organisationId) {
        return gateway().createOrGetCustomerForOrganisation(organisationId);
    }

    public void createSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier, boolean startTrial) {
        gateway().createSubscriptionForOrganisation(organisationId, tier, startTrial);
    }

    public void cancelSubscriptionForOrganisation(UUID organisationId, boolean immediate) {
        gateway().cancelSubscriptionForOrganisation(organisationId, immediate);
    }

    public List<InvoiceResponse> getInvoicesForOrganisation(UUID organisationId) {
        return gateway().getInvoicesForOrganisation(organisationId);
    }

    public PaymentMethodResponse attachPaymentMethod(UUID organisationId, String providerPaymentMethodId,
            boolean setAsDefault) {
        return gateway().attachPaymentMethod(organisationId, providerPaymentMethodId, setAsDefault);
    }

    public List<PaymentMethodResponse> getPaymentMethods(UUID organisationId) {
        return gateway().getPaymentMethods(organisationId);
    }

    public void removePaymentMethod(UUID organisationId, UUID paymentMethodId) {
        gateway().removePaymentMethod(organisationId, paymentMethodId);
    }

    public RefundResponse processRefund(UUID gymId, RefundRequest request) {
        return gateway().processRefund(gymId, request);
    }

    public List<RefundResponse> getRefundHistory(UUID gymId) {
        return gateway().getRefundHistory(gymId);
    }

    public RefundResponse getRefund(UUID gymId, UUID refundId) {
        return gateway().getRefund(gymId, refundId);
    }

    private PaymentGateway gateway() {
        return switch (paymentProviderProperties.getProvider()) {
            case STRIPE -> stripeGateway;
            case PAYSTACK -> paystackGateway;
            default -> throw new DomainException("PAYMENT_PROVIDER_NOT_SUPPORTED",
                    "Unsupported payment provider: " + paymentProviderProperties.getProvider());
        };
    }
}


