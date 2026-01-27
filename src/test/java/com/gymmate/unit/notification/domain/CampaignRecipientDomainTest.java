package com.gymmate.unit.notification.domain;

import com.gymmate.notification.domain.CampaignRecipient;
import com.gymmate.notification.domain.RecipientStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CampaignRecipient Domain Tests")
class CampaignRecipientDomainTest {

    @Nested
    @DisplayName("Recipient Creation Tests")
    class RecipientCreationTests {

        @Test
        @DisplayName("Should create recipient with builder and defaults")
        void createRecipient_WithBuilder_Success() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();

            // Act
            CampaignRecipient recipient = CampaignRecipient.builder()
                    .campaignId(campaignId)
                    .memberId(memberId)
                    .email("member@example.com")
                    .build();

            // Assert
            assertThat(recipient.getCampaignId()).isEqualTo(campaignId);
            assertThat(recipient.getMemberId()).isEqualTo(memberId);
            assertThat(recipient.getEmail()).isEqualTo("member@example.com");
            assertThat(recipient.getStatus()).isEqualTo(RecipientStatus.PENDING);
            assertThat(recipient.getSentAt()).isNull();
            assertThat(recipient.getDeliveredAt()).isNull();
            assertThat(recipient.getErrorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("Mark Sent Tests")
    class MarkSentTests {

        @Test
        @DisplayName("Should mark recipient as sent with timestamp")
        void markSent_UpdatesStatusAndTimestamp() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();

            // Act
            recipient.markSent();

            // Assert
            assertThat(recipient.getStatus()).isEqualTo(RecipientStatus.SENT);
            assertThat(recipient.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set sentAt timestamp close to now")
        void markSent_TimestampIsRecent() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();
            java.time.LocalDateTime before = java.time.LocalDateTime.now().minusSeconds(1);

            // Act
            recipient.markSent();

            // Assert
            assertThat(recipient.getSentAt()).isAfterOrEqualTo(before);
            assertThat(recipient.getSentAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now().plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("Mark Delivered Tests")
    class MarkDeliveredTests {

        @Test
        @DisplayName("Should mark recipient as delivered with timestamp")
        void markDelivered_UpdatesStatusAndTimestamp() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();

            // Act
            recipient.markDelivered();

            // Assert
            assertThat(recipient.getStatus()).isEqualTo(RecipientStatus.DELIVERED);
            assertThat(recipient.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should set deliveredAt timestamp close to now")
        void markDelivered_TimestampIsRecent() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();
            java.time.LocalDateTime before = java.time.LocalDateTime.now().minusSeconds(1);

            // Act
            recipient.markDelivered();

            // Assert
            assertThat(recipient.getDeliveredAt()).isAfterOrEqualTo(before);
            assertThat(recipient.getDeliveredAt()).isBeforeOrEqualTo(java.time.LocalDateTime.now().plusSeconds(1));
        }
    }

    @Nested
    @DisplayName("Mark Failed Tests")
    class MarkFailedTests {

        @Test
        @DisplayName("Should mark recipient as failed with error message")
        void markFailed_UpdatesStatusAndErrorMessage() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();
            String errorMessage = "SMTP connection refused";

            // Act
            recipient.markFailed(errorMessage);

            // Assert
            assertThat(recipient.getStatus()).isEqualTo(RecipientStatus.FAILED);
            assertThat(recipient.getErrorMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should store detailed error message")
        void markFailed_StoresDetailedError() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();
            String detailedError = "javax.mail.MessagingException: Could not connect to SMTP host: smtp.example.com, port: 587";

            // Act
            recipient.markFailed(detailedError);

            // Assert
            assertThat(recipient.getErrorMessage()).contains("MessagingException");
            assertThat(recipient.getErrorMessage()).contains("smtp.example.com");
        }

        @Test
        @DisplayName("Should allow null error message")
        void markFailed_NullErrorMessage_Success() {
            // Arrange
            CampaignRecipient recipient = createDefaultRecipient();

            // Act
            recipient.markFailed(null);

            // Assert
            assertThat(recipient.getStatus()).isEqualTo(RecipientStatus.FAILED);
            assertThat(recipient.getErrorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("RecipientStatus Tests")
    class RecipientStatusTests {

        @ParameterizedTest
        @EnumSource(RecipientStatus.class)
        @DisplayName("Should support all recipient statuses")
        void allStatuses_ShouldExist(RecipientStatus status) {
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should have all expected status values")
        void statusValues_ShouldBeCorrect() {
            assertThat(RecipientStatus.values())
                    .contains(
                            RecipientStatus.PENDING,
                            RecipientStatus.SENT,
                            RecipientStatus.DELIVERED,
                            RecipientStatus.FAILED);
        }
    }

    // Helper methods
    private CampaignRecipient createDefaultRecipient() {
        return CampaignRecipient.builder()
                .campaignId(UUID.randomUUID())
                .memberId(UUID.randomUUID())
                .email("test@example.com")
                .build();
    }
}
