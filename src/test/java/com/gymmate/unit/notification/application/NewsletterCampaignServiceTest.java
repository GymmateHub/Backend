package com.gymmate.unit.notification.application;

import com.gymmate.notification.api.dto.AudiencePreviewResponse;
import com.gymmate.notification.api.dto.CreateCampaignRequest;
import com.gymmate.notification.application.AudienceResolver;
import com.gymmate.notification.application.NewsletterCampaignService;
import com.gymmate.notification.application.NewsletterTemplateService;
import com.gymmate.notification.domain.*;
import com.gymmate.notification.infrastructure.CampaignRecipientRepository;
import com.gymmate.notification.infrastructure.NewsletterCampaignRepository;
import com.gymmate.notification.infrastructure.NewsletterTemplateRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsletterCampaignService Unit Tests")
class NewsletterCampaignServiceTest {

    @Mock
    private NewsletterCampaignRepository campaignRepository;

    @Mock
    private NewsletterTemplateRepository templateRepository;

    @Mock
    private CampaignRecipientRepository recipientRepository;

    @Mock
    private AudienceResolver audienceResolver;

    @Mock
    private NewsletterTemplateService templateService;

    @Mock
    private EmailService emailService;

    private NewsletterCampaignService campaignService;

    private UUID gymId;
    private UUID organisationId;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        campaignService = new NewsletterCampaignService(
                campaignRepository,
                templateRepository,
                recipientRepository,
                audienceResolver,
                templateService,
                emailService);
        gymId = UUID.randomUUID();
        organisationId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Create Campaign Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("Should create campaign without template")
        void create_WithoutTemplate_Success() {
            // Arrange
            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setGymId(gymId);
            request.setName("Monthly Newsletter");
            request.setSubject("Monthly Update");
            request.setBody("<p>Hello members!</p>");
            request.setAudienceType(AudienceType.ALL_MEMBERS);

            when(campaignRepository.save(any(NewsletterCampaign.class)))
                    .thenAnswer(inv -> {
                        NewsletterCampaign c = inv.getArgument(0);
                        c.setId(UUID.randomUUID());
                        return c;
                    });

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act
                NewsletterCampaign result = campaignService.create(request, createdBy);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Monthly Newsletter");
                assertThat(result.getSubject()).isEqualTo("Monthly Update");
                assertThat(result.getStatus()).isEqualTo(CampaignStatus.DRAFT);
            }
        }

        @Test
        @DisplayName("Should create campaign using template content")
        void create_WithTemplate_CopiesContent() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Welcome Template")
                    .subject("Template Subject")
                    .body("Template Body")
                    .build();
            template.setId(templateId);

            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setGymId(gymId);
            request.setTemplateId(templateId);
            request.setName("Welcome Campaign");
            request.setSubject(""); // Should use template subject
            request.setBody(""); // Should use template body
            request.setAudienceType(AudienceType.ALL_MEMBERS);

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(campaignRepository.save(any(NewsletterCampaign.class)))
                    .thenAnswer(inv -> {
                        NewsletterCampaign c = inv.getArgument(0);
                        c.setId(UUID.randomUUID());
                        return c;
                    });

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act
                NewsletterCampaign result = campaignService.create(request, createdBy);

                // Assert
                assertThat(result.getSubject()).isEqualTo("Template Subject");
                assertThat(result.getBody()).isEqualTo("Template Body");
                assertThat(result.getTemplateId()).isEqualTo(templateId);
            }
        }

        @Test
        @DisplayName("Should throw when template not found")
        void create_TemplateNotFound_ThrowsException() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setGymId(gymId);
            request.setTemplateId(templateId);
            request.setSubject("Subject");
            request.setBody("Body");
            request.setAudienceType(AudienceType.ALL_MEMBERS);

            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act & Assert
                assertThatThrownBy(() -> campaignService.create(request, createdBy))
                        .isInstanceOf(DomainException.class)
                        .hasFieldOrPropertyWithValue("errorCode", "TEMPLATE_NOT_FOUND");
            }
        }

        @Test
        @DisplayName("Should schedule campaign if scheduledAt provided")
        void create_WithScheduledAt_SchedulesCampaign() {
            // Arrange
            CreateCampaignRequest request = new CreateCampaignRequest();
            request.setGymId(gymId);
            request.setName("Scheduled Campaign");
            request.setSubject("Subject");
            request.setBody("Body");
            request.setAudienceType(AudienceType.ALL_MEMBERS);
            request.setScheduledAt(LocalDateTime.now().plusDays(1));

            when(campaignRepository.save(any(NewsletterCampaign.class)))
                    .thenAnswer(inv -> {
                        NewsletterCampaign c = inv.getArgument(0);
                        c.setId(UUID.randomUUID());
                        return c;
                    });

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act
                NewsletterCampaign result = campaignService.create(request, createdBy);

                // Assert
                assertThat(result.getStatus()).isEqualTo(CampaignStatus.SCHEDULED);
                assertThat(result.getScheduledAt()).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Get Campaign Tests")
    class GetCampaignTests {

        @Test
        @DisplayName("Should get campaign by ID")
        void getById_Exists_ReturnsCampaign() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

            // Act
            NewsletterCampaign result = campaignService.getById(campaignId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(campaignId);
        }

        @Test
        @DisplayName("Should throw when campaign not found")
        void getById_NotFound_ThrowsException() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> campaignService.getById(campaignId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_NOT_FOUND");
        }

        @Test
        @DisplayName("Should get campaigns by gym ID")
        void getByGymId_ReturnsList() {
            // Arrange
            NewsletterCampaign c1 = createExistingCampaign(UUID.randomUUID());
            NewsletterCampaign c2 = createExistingCampaign(UUID.randomUUID());

            when(campaignRepository.findByGymId(gymId)).thenReturn(List.of(c1, c2));

            // Act
            List<NewsletterCampaign> results = campaignService.getByGymId(gymId);

            // Assert
            assertThat(results).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Schedule Campaign Tests")
    class ScheduleCampaignTests {

        @Test
        @DisplayName("Should schedule draft campaign")
        void schedule_DraftCampaign_Success() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            LocalDateTime scheduledTime = LocalDateTime.now().plusDays(1);

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            NewsletterCampaign result = campaignService.schedule(campaignId, scheduledTime);

            // Assert
            assertThat(result.getStatus()).isEqualTo(CampaignStatus.SCHEDULED);
            assertThat(result.getScheduledAt()).isEqualTo(scheduledTime);
        }
    }

    @Nested
    @DisplayName("Cancel Campaign Tests")
    class CancelCampaignTests {

        @Test
        @DisplayName("Should cancel scheduled campaign")
        void cancel_ScheduledCampaign_Success() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            campaign.schedule(LocalDateTime.now().plusDays(1));

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            NewsletterCampaign result = campaignService.cancel(campaignId);

            // Assert
            assertThat(result.getStatus()).isEqualTo(CampaignStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Get Audience Preview Tests")
    class GetAudiencePreviewTests {

        @Test
        @DisplayName("Should return audience preview")
        void getAudiencePreview_ReturnsCounts() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            campaign.setGymId(gymId);

            AudiencePreviewResponse preview = AudiencePreviewResponse.builder()
                    .totalCount(150)
                    .build();

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(audienceResolver.getAudiencePreview(gymId, AudienceType.ALL_MEMBERS, null))
                    .thenReturn(preview);

            // Act
            AudiencePreviewResponse result = campaignService.getAudiencePreview(campaignId);

            // Assert
            assertThat(result.getTotalCount()).isEqualTo(150);
        }
    }

    @Nested
    @DisplayName("Send Campaign Tests")
    class SendCampaignTests {

        @Test
        @DisplayName("Should send draft campaign")
        void send_DraftCampaign_Success() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            UUID sentByUserId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            // Mock empty audience so async completes quickly
            when(audienceResolver.resolveAudience(any(), any(), any())).thenReturn(List.of());

            // Act
            NewsletterCampaign result = campaignService.send(campaignId, sentByUserId);

            // Assert - After async completes with 0 recipients it will be SENT or FAILED
            // Verify that sentByUserId was set and campaign repository was called
            assertThat(result.getSentByUserId()).isEqualTo(sentByUserId);
            // Verify campaign was saved at least twice (once for SENDING, once for
            // complete)
            verify(campaignRepository, atLeast(2)).save(any(NewsletterCampaign.class));
        }

        @Test
        @DisplayName("Should reject sending already sent campaign")
        void send_AlreadySentCampaign_ThrowsException() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            campaign.startSending();
            campaign.completeSending(10, 10, 0);

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

            // Act & Assert
            assertThatThrownBy(() -> campaignService.send(campaignId, UUID.randomUUID()))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_CANNOT_SEND");
        }
    }

    @Nested
    @DisplayName("Delete Campaign Tests")
    class DeleteCampaignTests {

        @Test
        @DisplayName("Should soft delete draft campaign")
        void delete_DraftCampaign_Success() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            campaign.setActive(true);

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            campaignService.delete(campaignId);

            // Assert
            ArgumentCaptor<NewsletterCampaign> captor = ArgumentCaptor.forClass(NewsletterCampaign.class);
            verify(campaignRepository).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }

        @Test
        @DisplayName("Should reject deleting campaign in sending state")
        void delete_SendingCampaign_ThrowsException() {
            // Arrange
            UUID campaignId = UUID.randomUUID();
            NewsletterCampaign campaign = createExistingCampaign(campaignId);
            campaign.startSending();

            when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));

            // Act & Assert
            assertThatThrownBy(() -> campaignService.delete(campaignId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "CAMPAIGN_IN_PROGRESS");
        }
    }

    // Helper methods
    private NewsletterCampaign createExistingCampaign(UUID id) {
        NewsletterCampaign campaign = NewsletterCampaign.builder()
                .name("Test Campaign")
                .subject("Test Subject")
                .body("<p>Test Body</p>")
                .audienceType(AudienceType.ALL_MEMBERS)
                .build();
        campaign.setId(id);
        campaign.setGymId(gymId);
        return campaign;
    }
}
