package com.gymmate.unit.notification.application;

import com.gymmate.notification.application.EmailSuppressionService;
import com.gymmate.notification.application.SesWebhookService;
import com.gymmate.notification.domain.SuppressionReason;
import com.gymmate.notification.internal.web.SesWebhookController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SesWebhookController Unit Tests")
class SesWebhookControllerTest {

    @Mock
    private SesWebhookService sesWebhookService;

    @Mock
    private EmailSuppressionService suppressionService;

    private SesWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new SesWebhookController(sesWebhookService, suppressionService);
    }

    @Test
    @DisplayName("Should return 200 OK for SES event webhook")
    void shouldHandleSesEventWebhook() {
        String payload = "{\"Type\":\"Notification\"}";
        ResponseEntity<String> response = controller.handleSesEvent(payload);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("OK");
        verify(sesWebhookService).processSnsPayload(payload);
    }

    @Test
    @DisplayName("Should process one-click unsubscribe and suppress email")
    void shouldHandleOneClickUnsubscribe() {
        ResponseEntity<String> response = controller.handleOneClickUnsubscribe("unsub@example.com", "token123");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(suppressionService).suppressPermanent(
                eq("unsub@example.com"),
                eq(SuppressionReason.UNSUBSCRIBED),
                eq("Unsubscribed"),
                eq("OneClick"),
                anyString(),
                eq(null),
                eq(null));
    }
}
