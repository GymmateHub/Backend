package com.gymmate.payment.application;

import com.gymmate.payment.api.dto.RefundRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceRoutingTest {

    @Mock
    private PaymentGateway primaryGateway;

    @Mock
    private PaymentGateway secondaryGateway;

    private PaymentProviderProperties props;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        props = new PaymentProviderProperties();
        paymentService = new PaymentService(props, primaryGateway, secondaryGateway);
    }

    @Test
    void routesToSecondaryGatewayWhenConfigured() {
        props.setProvider(PaymentProviderProperties.Provider.PAYSTACK);

        RefundRequest request = RefundRequest.builder()
                .providerTransactionId("txn_123")
                .build();

        paymentService.processRefund(UUID.randomUUID(), request);

        verify(secondaryGateway, times(1)).processRefund(any(), any());
        verify(primaryGateway, times(0)).processRefund(any(), any());
    }

    @Test
    void routesToPrimaryGatewayWhenConfigured() {
        props.setProvider(PaymentProviderProperties.Provider.STRIPE);

        RefundRequest request = RefundRequest.builder()
                .providerTransactionId("txn_123")
                .build();

        paymentService.processRefund(UUID.randomUUID(), request);

        verify(primaryGateway, times(1)).processRefund(any(), any());
        verify(secondaryGateway, times(0)).processRefund(any(), any());
    }
}

