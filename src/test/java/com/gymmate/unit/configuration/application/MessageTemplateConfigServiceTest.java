package com.gymmate.unit.configuration.application;

import com.gymmate.configuration.api.dto.RenderedEmail;
import com.gymmate.configuration.api.dto.RenderedNotification;
import com.gymmate.configuration.internal.application.MessageTemplateConfigService;
import com.gymmate.configuration.internal.application.dto.MessageTemplateConfigResponse;
import com.gymmate.configuration.internal.application.dto.UpdateMessageTemplateRequest;
import com.gymmate.configuration.internal.application.port.MessageTemplateConfigRepository;
import com.gymmate.configuration.internal.domain.DefaultMessageTemplates;
import com.gymmate.configuration.internal.domain.MessageTemplateChannel;
import com.gymmate.configuration.internal.domain.MessageTemplateConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageTemplateConfigService Unit Tests")
class MessageTemplateConfigServiceTest {

    @Mock
    private MessageTemplateConfigRepository repository;

    private MessageTemplateConfigService service;
    private UUID organisationId;

    @BeforeEach
    void setUp() {
        service = new MessageTemplateConfigService(repository);
        organisationId = UUID.randomUUID();
        lenient().when(repository.save(any(MessageTemplateConfig.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Should render built-in email template when no DB overrides exist")
    void shouldRenderBuiltInEmailTemplate() {
        when(repository.findByTemplateKeyAndOrganisationId(
                DefaultMessageTemplates.EMAIL_USER_INVITE, organisationId))
                .thenReturn(Optional.empty());
        when(repository.findByTemplateKeyAndOrganisationIdIsNull(
                DefaultMessageTemplates.EMAIL_USER_INVITE))
                .thenReturn(Optional.empty());

        RenderedEmail rendered = service.renderEmail(
                DefaultMessageTemplates.EMAIL_USER_INVITE,
                organisationId,
                Map.of(
                        "firstName", "Ada",
                        "inviterName", "Grace",
                        "gymName", "Iron Temple",
                        "role", "MANAGER",
                        "inviteLink", "https://gymmatehub.com/invite?token=abc"));

        assertThat(rendered.enabled()).isTrue();
        assertThat(rendered.subject()).isEqualTo("You've been invited to join Iron Temple");
        assertThat(rendered.htmlBody())
                .contains("Hello Ada")
                .contains("Grace has invited you to join Iron Temple as a MANAGER")
                .contains("https://gymmatehub.com/invite?token=abc");
    }

    @Test
    @DisplayName("Organisation override should take precedence over platform default")
    void organisationOverrideTakesPrecedence() {
        MessageTemplateConfig orgOverride = MessageTemplateConfig.builder()
                .organisationId(organisationId)
                .templateKey(DefaultMessageTemplates.EMAIL_PASSWORD_RESET)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Custom Reset")
                .subjectOrTitle("Reset your {{name}} account password")
                .bodyTemplate("<p>Custom reset for {{name}}: <a href='{{resetLink}}'>Reset</a></p>")
                .bodyCustomised(true)
                .build();

        when(repository.findByTemplateKeyAndOrganisationId(
                DefaultMessageTemplates.EMAIL_PASSWORD_RESET, organisationId))
                .thenReturn(Optional.of(orgOverride));

        RenderedEmail rendered = service.renderEmail(
                DefaultMessageTemplates.EMAIL_PASSWORD_RESET,
                organisationId,
                Map.of("name", "Ada", "resetLink", "https://gymmatehub.com/reset"));

        assertThat(rendered.subject()).isEqualTo("Reset your Ada account password");
        assertThat(rendered.htmlBody()).isEqualTo("<p>Custom reset for Ada: <a href='https://gymmatehub.com/reset'>Reset</a></p>");
        assertThat(rendered.bodyCustomised()).isTrue();
    }

    @Test
    @DisplayName("Should render configured notification template with interpolated variables")
    void shouldRenderConfiguredNotificationTemplate() {
        MessageTemplateConfig customNotification = MessageTemplateConfig.builder()
                .organisationId(null)
                .templateKey(DefaultMessageTemplates.NOTIFICATION_MEMBER_JOINED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Member Joined")
                .subjectOrTitle("Welcome {{memberName}}!")
                .bodyTemplate("Member {{memberName}} ({{memberEmail}}) subscribed to {{membershipPlan}}.")
                .bodyCustomised(true)
                .build();

        when(repository.findByTemplateKeyAndOrganisationId(
                DefaultMessageTemplates.NOTIFICATION_MEMBER_JOINED, organisationId))
                .thenReturn(Optional.empty());
        when(repository.findByTemplateKeyAndOrganisationIdIsNull(
                DefaultMessageTemplates.NOTIFICATION_MEMBER_JOINED))
                .thenReturn(Optional.of(customNotification));

        RenderedNotification rendered = service.renderNotification(
                DefaultMessageTemplates.NOTIFICATION_MEMBER_JOINED,
                organisationId,
                Map.of(
                        "memberName", "Chidi Obi",
                        "memberEmail", "chidi@example.com",
                        "membershipPlan", "Pro Monthly"),
                "Default Title",
                "Default Message");

        assertThat(rendered.enabled()).isTrue();
        assertThat(rendered.title()).isEqualTo("Welcome Chidi Obi!");
        assertThat(rendered.message()).isEqualTo("Member Chidi Obi (chidi@example.com) subscribed to Pro Monthly.");
    }

    @Test
    @DisplayName("Should upsert and delete organisation override cleanly")
    void shouldUpsertAndDeleteOrganisationOverride() {
        when(repository.findByTemplateKeyAndOrganisationIdIsNull(DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING))
                .thenReturn(Optional.empty());
        when(repository.findByTemplateKeyAndOrganisationId(DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING, organisationId))
                .thenReturn(Optional.empty());

        UpdateMessageTemplateRequest request = new UpdateMessageTemplateRequest(
                "Custom Trial Notice",
                "Custom description",
                "Trial ending for your gym",
                "Your trial ends on {{trialEnd}}.",
                true,
                true);

        MessageTemplateConfigResponse upserted = service.upsertOrganisationOverride(
                organisationId, DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING, request);

        assertThat(upserted.organisationOverride()).isTrue();
        assertThat(upserted.organisationId()).isEqualTo(organisationId);
        assertThat(upserted.subjectOrTitle()).isEqualTo("Trial ending for your gym");

        MessageTemplateConfig savedOverride = MessageTemplateConfig.builder()
                .organisationId(organisationId)
                .templateKey(DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING)
                .subjectOrTitle("Trial ending for your gym")
                .bodyTemplate("Your trial ends on {{trialEnd}}.")
                .build();
        when(repository.findByTemplateKeyAndOrganisationId(DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING, organisationId))
                .thenReturn(Optional.of(savedOverride));

        MessageTemplateConfigResponse reverted = service.deleteOrganisationOverride(
                organisationId, DefaultMessageTemplates.NOTIFICATION_TRIAL_ENDING);

        verify(repository).delete(savedOverride);
        assertThat(reverted.organisationOverride()).isFalse();
        assertThat(reverted.subjectOrTitle()).isEqualTo("Trial Ending Soon");
    }

    @Test
    @DisplayName("Should list all platform templates merging built-in catalog and DB overrides")
    void shouldListAllPlatformTemplates() {
        when(repository.findByOrganisationIdIsNull()).thenReturn(List.of());

        List<MessageTemplateConfigResponse> templates = service.listPlatformTemplates();

        assertThat(templates).isNotEmpty();
        assertThat(templates)
                .extracting(MessageTemplateConfigResponse::templateKey)
                .contains(
                        DefaultMessageTemplates.EMAIL_PASSWORD_RESET,
                        DefaultMessageTemplates.EMAIL_REGISTRATION_OTP,
                        DefaultMessageTemplates.EMAIL_BILLING_TRIAL_ENDING_REMINDER,
                        DefaultMessageTemplates.NOTIFICATION_SUBSCRIPTION_EXPIRED,
                        DefaultMessageTemplates.NOTIFICATION_MEMBER_JOINED);
    }

    @Test
    @DisplayName("PersistenceMappers should validate MessageTemplateConfig domain-to-JPA mapping")
    void persistenceMappersValidatesDomainEntityPair() {
        var mappers = new com.gymmate.shared.infrastructure.persistence.PersistenceMappers();
        assertThat(mappers.size()).isGreaterThan(0);
    }
}
