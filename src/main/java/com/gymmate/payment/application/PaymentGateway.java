package com.gymmate.payment.application;

import com.gymmate.payment.api.dto.InvoiceResponse;
import com.gymmate.payment.api.dto.PaymentMethodResponse;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.subscription.domain.SubscriptionTier;

import java.util.List;
import java.util.UUID;

/**
 * Provider-agnostic payment port for platform billing operations.
 */
public interface PaymentGateway {

    String providerName();

    String createOrGetCustomerForOrganisation(UUID organisationId);

    void createSubscriptionForOrganisation(UUID organisationId, SubscriptionTier tier, boolean startTrial);

    void cancelSubscriptionForOrganisation(UUID organisationId, boolean immediate);

    List<InvoiceResponse> getInvoicesForOrganisation(UUID organisationId);

    PaymentMethodResponse attachPaymentMethod(UUID organisationId, String providerPaymentMethodId, boolean setAsDefault);

    List<PaymentMethodResponse> getPaymentMethods(UUID organisationId);

    void removePaymentMethod(UUID organisationId, UUID paymentMethodId);

    RefundResponse processRefund(UUID gymId, RefundRequest request);

    List<RefundResponse> getRefundHistory(UUID gymId);

    RefundResponse getRefund(UUID gymId, UUID refundId);
}

