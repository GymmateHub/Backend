package com.gymmate.unit.notification.domain;

import com.gymmate.notification.domain.AudienceType;
import com.gymmate.notification.domain.CampaignStatus;
import com.gymmate.notification.domain.NewsletterCampaign;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NewsletterCampaign Domain Tests")
class NewsletterCampaignDomainTest {

    @Nested
    @DisplayName("Campaign Creation Tests")
    class CampaignCreationTests {

        @Test
        @DisplayName("Should create campaign with builder and defaults")
        void createCampaign_WithBuilder_Success() {
            // Arrange & Act
            NewsletterCampaign campaign = NewsletterCampaign.builder()
                    .name("Welcome Campaign")
                    .subject("Welcome to our gym!")
                    .body("<h1>Welcome!</h1><p>Thanks for joining.</p>")
                    .audienceType(AudienceType.ALL_MEMBERS)
                    .build();

            // Assert
            assertThat(campaign.getName()).isEqualTo("Welcome Campaign");
            assertThat(campaign.getSubject()).isEqualTo("Welcome to our gym!");
            assertThat(campaign.getAudienceType()).isEqualTo(AudienceType.ALL_MEMBERS);
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.DRAFT);
            assertThat(campaign.getTotalRecipients()).isZero();
            assertThat(campaign.getDeliveredCount()).isZero();
            assertThat(campaign.getFailedCount()).isZero();
        }

        @Test
        @DisplayName("Should create campaign with template reference")
        void createCampaign_WithTemplateId_Success() {
            // Arrange
            UUID templateId = UUID.randomUUID();

            // Act
            NewsletterCampaign campaign = NewsletterCampaign.builder()
                    .templateId(templateId)
                    .name("Monthly Newsletter")
                    .subject("Monthly Update")
                    .body("Content here")
                    .audienceType(AudienceType.ALL_MEMBERS)
                    .build();

            // Assert
            assertThat(campaign.getTemplateId()).isEqualTo(templateId);
        }
    }

    @Nested
    @DisplayName("Schedule Tests")
    class ScheduleTests {

        @Test
        @DisplayName("Should schedule draft campaign for future delivery")
        void schedule_DraftCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

            // Act
            campaign.schedule(futureTime);

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.SCHEDULED);
            assertThat(campaign.getScheduledAt()).isEqualTo(futureTime);
        }

        @Test
        @DisplayName("Should reject scheduling non-draft campaign")
        void schedule_NonDraftCampaign_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.schedule(LocalDateTime.now().plusDays(1));
            LocalDateTime newTime = LocalDateTime.now().plusDays(2);

            // Act & Assert
            assertThatThrownBy(() -> campaign.schedule(newTime))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_NOT_DRAFT");
        }

        @Test
        @DisplayName("Should reject scheduling for past time")
        void schedule_PastTime_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

            // Act & Assert
            assertThatThrownBy(() -> campaign.schedule(pastTime))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "INVALID_SCHEDULE_TIME");
        }
    }

    @Nested
    @DisplayName("Start Sending Tests")
    class StartSendingTests {

        @Test
        @DisplayName("Should start sending from draft status")
        void startSending_DraftCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();

            // Act
            campaign.startSending();

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.SENDING);
        }

        @Test
        @DisplayName("Should start sending from scheduled status")
        void startSending_ScheduledCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.schedule(LocalDateTime.now().plusDays(1));

            // Act
            campaign.startSending();

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.SENDING);
        }

        @Test
        @DisplayName("Should reject starting already sent campaign")
        void startSending_SentCampaign_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();
            campaign.completeSending(10, 10, 0);

            // Act & Assert
            assertThatThrownBy(() -> campaign.startSending())
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_CANNOT_SEND");
        }
    }

    @Nested
    @DisplayName("Complete Sending Tests")
    class CompleteSendingTests {

        @Test
        @DisplayName("Should mark campaign as SENT when deliveries succeed")
        void completeSending_AllDelivered_StatusSent() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();

            // Act
            campaign.completeSending(100, 95, 5);

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.SENT);
            assertThat(campaign.getTotalRecipients()).isEqualTo(100);
            assertThat(campaign.getDeliveredCount()).isEqualTo(95);
            assertThat(campaign.getFailedCount()).isEqualTo(5);
            assertThat(campaign.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("Should mark campaign as FAILED when all deliveries fail")
        void completeSending_AllFailed_StatusFailed() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();

            // Act
            campaign.completeSending(50, 0, 50);

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.FAILED);
            assertThat(campaign.getTotalRecipients()).isEqualTo(50);
            assertThat(campaign.getDeliveredCount()).isZero();
            assertThat(campaign.getFailedCount()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Cancel Tests")
    class CancelTests {

        @Test
        @DisplayName("Should cancel draft campaign")
        void cancel_DraftCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();

            // Act
            campaign.cancel();

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should cancel scheduled campaign")
        void cancel_ScheduledCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.schedule(LocalDateTime.now().plusDays(1));

            // Act
            campaign.cancel();

            // Assert
            assertThat(campaign.getStatus()).isEqualTo(CampaignStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should reject cancelling sent campaign")
        void cancel_SentCampaign_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();
            campaign.completeSending(10, 10, 0);

            // Act & Assert
            assertThatThrownBy(() -> campaign.cancel())
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_CANNOT_CANCEL");
        }

        @Test
        @DisplayName("Should reject cancelling campaign in sending state")
        void cancel_SendingCampaign_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();

            // Act & Assert
            assertThatThrownBy(() -> campaign.cancel())
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_CANNOT_CANCEL");
        }
    }

    @Nested
    @DisplayName("Update Content Tests")
    class UpdateContentTests {

        @Test
        @DisplayName("Should update content of draft campaign")
        void updateContent_DraftCampaign_Success() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();

            // Act
            campaign.updateContent("Updated Name", "Updated Subject", "Updated Body");

            // Assert
            assertThat(campaign.getName()).isEqualTo("Updated Name");
            assertThat(campaign.getSubject()).isEqualTo("Updated Subject");
            assertThat(campaign.getBody()).isEqualTo("Updated Body");
        }

        @Test
        @DisplayName("Should reject updating non-draft campaign")
        void updateContent_NonDraftCampaign_ThrowsException() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.schedule(LocalDateTime.now().plusDays(1));

            // Act & Assert
            assertThatThrownBy(() -> campaign.updateContent("New", "New", "New"))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_NOT_DRAFT");
        }
    }

    @Nested
    @DisplayName("Can Send Tests")
    class CanSendTests {

        @Test
        @DisplayName("Draft campaign can be sent")
        void canSend_DraftCampaign_ReturnsTrue() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();

            // Act & Assert
            assertThat(campaign.canSend()).isTrue();
        }

        @Test
        @DisplayName("Scheduled campaign can be sent")
        void canSend_ScheduledCampaign_ReturnsTrue() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.schedule(LocalDateTime.now().plusDays(1));

            // Act & Assert
            assertThat(campaign.canSend()).isTrue();
        }

        @Test
        @DisplayName("Sent campaign cannot be sent again")
        void canSend_SentCampaign_ReturnsFalse() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.startSending();
            campaign.completeSending(10, 10, 0);

            // Act & Assert
            assertThat(campaign.canSend()).isFalse();
        }

        @Test
        @DisplayName("Cancelled campaign cannot be sent")
        void canSend_CancelledCampaign_ReturnsFalse() {
            // Arrange
            NewsletterCampaign campaign = createDraftCampaign();
            campaign.cancel();

            // Act & Assert
            assertThat(campaign.canSend()).isFalse();
        }
    }

    @Nested
    @DisplayName("AudienceType Tests")
    class AudienceTypeTests {

        @ParameterizedTest
        @EnumSource(AudienceType.class)
        @DisplayName("Should support all audience types")
        void allAudienceTypes_ShouldExist(AudienceType audienceType) {
            // Arrange & Act
            NewsletterCampaign campaign = NewsletterCampaign.builder()
                    .name("Test")
                    .subject("Test")
                    .body("Test")
                    .audienceType(audienceType)
                    .build();

            // Assert
            assertThat(campaign.getAudienceType()).isEqualTo(audienceType);
        }
    }

    // Helper methods
    private NewsletterCampaign createDraftCampaign() {
        return NewsletterCampaign.builder()
                .name("Test Campaign")
                .subject("Test Subject")
                .body("<p>Test body</p>")
                .audienceType(AudienceType.ALL_MEMBERS)
                .build();
    }
}
