package com.gymmate.payment.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.payment.domain.PaymentWebhookEvent;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.payment.infrastructure.PaymentWebhookEventRepository;
import com.gymmate.notification.events.PaymentSuccessEvent;
import com.gymmate.shared.config.PaystackConfig;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaystackWebhookServiceTest {

    @Mock
    private PaystackConfig paystackConfig;

    @Mock
    private PaymentWebhookEventRepository webhookEventRepository;

    @Mock
    private MemberMembershipJpaRepository memberMembershipRepository;

    @Mock
    private MemberInvoiceRepository memberInvoiceRepository;

    @Mock
    private PaymentRefundRepository paymentRefundRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PaystackWebhookService service;

    @BeforeEach
    void setUp() {
        service = new PaystackWebhookService(
                paystackConfig,
                new ObjectMapper(),
                webhookEventRepository,
                memberMembershipRepository,
                memberInvoiceRepository,
                paymentRefundRepository,
                eventPublisher
        );
    }

    @Test
    void processesChargeSuccessAndStoresWebhookEvent() throws Exception {
        when(paystackConfig.isConfigured()).thenReturn(true);
        when(paystackConfig.getSecretKey()).thenReturn("secret_123");
        when(webhookEventRepository.existsByProviderAndProviderEventId(any(), any())).thenReturn(false);

        UUID memberId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();

        MemberMembership membership = MemberMembership.builder()
                .memberId(memberId)
                .membershipPlanId(UUID.randomUUID())
                .startDate(LocalDate.now())
                .monthlyAmount(BigDecimal.TEN)
                .billingCycle("monthly")
                .status(MembershipStatus.PAST_DUE)
                .build();
        membership.setId(membershipId);
        membership.setOrganisationId(orgId);
        membership.setGymId(gymId);

        when(memberMembershipRepository.findById(membershipId)).thenReturn(Optional.of(membership));

        String payload = """
                {
                  "event": "charge.success",
                  "data": {
                    "id": 100200,
                    "reference": "ref_123",
                    "amount": 950000,
                    "currency": "NGN",
                    "metadata": {
                      "membership_id": "%s",
                      "gym_id": "%s",
                      "organisation_id": "%s"
                    }
                  }
                }
                """.formatted(membershipId, gymId, orgId);

        String signature = sign(payload, "secret_123");
        service.processWebhook(payload, signature);

        verify(memberMembershipRepository).save(any(MemberMembership.class));
        verify(memberInvoiceRepository).save(any());
        verify(eventPublisher).publishEvent(any(PaymentSuccessEvent.class));

        ArgumentCaptor<PaymentWebhookEvent> captor = ArgumentCaptor.forClass(PaymentWebhookEvent.class);
        verify(webhookEventRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getProvider()).isEqualTo("paystack");
    }

    @Test
    void rejectsInvalidSignature() {
        when(paystackConfig.isConfigured()).thenReturn(true);
        when(paystackConfig.getSecretKey()).thenReturn("secret_123");

        String payload = "{" + "\"event\":\"charge.success\",\"data\":{\"id\":1}}";

        assertThatThrownBy(() -> service.processWebhook(payload, "bad_signature"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid Paystack signature");
    }

    private String sign(String payload, String secret) throws Exception {
        Mac sha512Hmac = Mac.getInstance("HmacSHA512");
        sha512Hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hash = sha512Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

