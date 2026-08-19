package com.gymmate.unit.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.notification.application.EmailSuppressionService;
import com.gymmate.notification.application.SesWebhookService;
import com.gymmate.notification.application.SnsSignatureVerifier;
import com.gymmate.notification.domain.SnsProcessedMessage;
import com.gymmate.notification.domain.SuppressionReason;
import com.gymmate.notification.infrastructure.SnsProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SesWebhookService Unit Tests")
class SesWebhookServiceTest {

    @Mock
    private SnsSignatureVerifier signatureVerifier;

    @Mock
    private SnsProcessedMessageRepository processedMessageRepository;

    @Mock
    private EmailSuppressionService suppressionService;

    private SesWebhookService webhookService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        webhookService = new SesWebhookService(
                objectMapper,
                signatureVerifier,
                processedMessageRepository,
                suppressionService);
    }

    @Nested
    @DisplayName("Hard Bounce Processing Tests")
    class HardBounceTests {

        @Test
        @DisplayName("Should process permanent bounce and suppress recipient email")
        void shouldProcessPermanentBounce() {
            String bounceMessage = """
                {
                    "eventType": "Bounce",
                    "bounce": {
                        "bounceType": "Permanent",
                        "bounceSubType": "General",
                        "bouncedRecipients": [
                            {
                                "emailAddress": "hardbounce@simulator.amazonses.com",
                                "diagnosticCode": "smtp; 550 5.1.1 User unknown"
                            }
                        ]
                    },
                    "mail": {
                        "messageId": "ses-msg-12345"
                    }
                }
                """;

            String snsPayload = """
                {
                    "Type": "Notification",
                    "MessageId": "sns-msg-99999",
                    "TopicArn": "arn:aws:sns:us-east-2:123456789012:gymmatehub-ses-events",
                    "Message": %s,
                    "Timestamp": "2026-08-19T10:00:00.000Z",
                    "SignatureVersion": "1",
                    "Signature": "mockSignature",
                    "SigningCertURL": "https://sns.us-east-2.amazonaws.com/SimpleNotificationService-12345.pem"
                }
                """.formatted(objectMapper.valueToTree(bounceMessage).toString());

            when(processedMessageRepository.existsByMessageId("sns-msg-99999")).thenReturn(false);
            when(signatureVerifier.verifySignature(any())).thenReturn(true);

            webhookService.processSnsPayload(snsPayload);

            verify(suppressionService).suppressPermanent(
                    eq("hardbounce@simulator.amazonses.com"),
                    eq(SuppressionReason.PERMANENT_BOUNCE),
                    eq("Permanent"),
                    eq("General"),
                    eq("smtp; 550 5.1.1 User unknown"),
                    isNull(),
                    isNull());

            verify(processedMessageRepository).save(any(SnsProcessedMessage.class));
        }
    }

    @Nested
    @DisplayName("Complaint Processing Tests")
    class ComplaintTests {

        @Test
        @DisplayName("Should process spam complaint and suppress recipient permanently")
        void shouldProcessComplaint() {
            String complaintMessage = """
                {
                    "eventType": "Complaint",
                    "complaint": {
                        "complaintFeedbackType": "abuse",
                        "complainedRecipients": [
                            {
                                "emailAddress": "complaint@simulator.amazonses.com"
                            }
                        ]
                    },
                    "mail": {
                        "messageId": "ses-msg-complaint-1"
                    }
                }
                """;

            String snsPayload = """
                {
                    "Type": "Notification",
                    "MessageId": "sns-complaint-msg",
                    "TopicArn": "arn:aws:sns:us-east-2:123456789012:gymmatehub-ses-events",
                    "Message": %s,
                    "Timestamp": "2026-08-19T10:00:00.000Z",
                    "SignatureVersion": "1",
                    "Signature": "mockSignature",
                    "SigningCertURL": "https://sns.us-east-2.amazonaws.com/SimpleNotificationService-12345.pem"
                }
                """.formatted(objectMapper.valueToTree(complaintMessage).toString());

            when(processedMessageRepository.existsByMessageId("sns-complaint-msg")).thenReturn(false);
            when(signatureVerifier.verifySignature(any())).thenReturn(true);

            webhookService.processSnsPayload(snsPayload);

            verify(suppressionService).suppressPermanent(
                    eq("complaint@simulator.amazonses.com"),
                    eq(SuppressionReason.COMPLAINT),
                    eq("Complaint"),
                    eq("abuse"),
                    contains("abuse"),
                    isNull(),
                    isNull());
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should skip processing if SNS messageId has already been processed")
        void shouldSkipAlreadyProcessedMessage() {
            String snsPayload = """
                {
                    "Type": "Notification",
                    "MessageId": "sns-already-seen",
                    "TopicArn": "arn:aws:sns:us-east-2:123456789012:gymmatehub-ses-events",
                    "Message": "{}",
                    "SignatureVersion": "1",
                    "Signature": "mock",
                    "SigningCertURL": "https://sns.us-east-2.amazonaws.com/cert.pem"
                }
                """;

            when(processedMessageRepository.existsByMessageId("sns-already-seen")).thenReturn(true);

            webhookService.processSnsPayload(snsPayload);

            verifyNoInteractions(signatureVerifier);
            verifyNoInteractions(suppressionService);
            verify(processedMessageRepository, never()).save(any());
        }
    }
}
