package com.gymmate.unit.notification.application;

import com.gymmate.notification.api.dto.CreateTemplateRequest;
import com.gymmate.notification.api.dto.UpdateTemplateRequest;
import com.gymmate.notification.domain.NewsletterTemplate;
import com.gymmate.notification.infrastructure.NewsletterTemplateRepository;
import com.gymmate.notification.application.NewsletterTemplateService;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NewsletterTemplateService Unit Tests")
class NewsletterTemplateServiceTest {

    @Mock
    private NewsletterTemplateRepository templateRepository;

    private NewsletterTemplateService templateService;

    private UUID gymId;
    private UUID organisationId;
    private UUID createdBy;

    @BeforeEach
    void setUp() {
        templateService = new NewsletterTemplateService(templateRepository);
        gymId = UUID.randomUUID();
        organisationId = UUID.randomUUID();
        createdBy = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Create Template Tests")
    class CreateTemplateTests {

        @Test
        @DisplayName("Should create template successfully")
        void create_ValidRequest_Success() {
            // Arrange
            CreateTemplateRequest request = new CreateTemplateRequest();
            request.setGymId(gymId);
            request.setName("Welcome Email");
            request.setSubject("Welcome {{first_name}}!");
            request.setBody("<h1>Welcome!</h1>");
            request.setTemplateType("EMAIL");
            request.setPlaceholders("[\"first_name\"]");

            when(templateRepository.existsByGymIdAndName(gymId, "Welcome Email")).thenReturn(false);
            when(templateRepository.save(any(NewsletterTemplate.class)))
                    .thenAnswer(inv -> {
                        NewsletterTemplate t = inv.getArgument(0);
                        t.setId(UUID.randomUUID());
                        return t;
                    });

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act
                NewsletterTemplate result = templateService.create(request, createdBy);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Welcome Email");
                assertThat(result.getSubject()).isEqualTo("Welcome {{first_name}}!");
                assertThat(result.getGymId()).isEqualTo(gymId);

                ArgumentCaptor<NewsletterTemplate> captor = ArgumentCaptor.forClass(NewsletterTemplate.class);
                verify(templateRepository).save(captor.capture());
                assertThat(captor.getValue().getCreatedBy()).isEqualTo(createdBy.toString());
            }
        }

        @Test
        @DisplayName("Should reject duplicate template name for same gym")
        void create_DuplicateName_ThrowsException() {
            // Arrange
            CreateTemplateRequest request = new CreateTemplateRequest();
            request.setGymId(gymId);
            request.setName("Existing Template");
            request.setSubject("Subject");
            request.setBody("Body");

            when(templateRepository.existsByGymIdAndName(gymId, "Existing Template")).thenReturn(true);

            try (MockedStatic<TenantContext> tenantMock = mockStatic(TenantContext.class)) {
                tenantMock.when(TenantContext::getCurrentTenantId).thenReturn(organisationId);

                // Act & Assert
                assertThatThrownBy(() -> templateService.create(request, createdBy))
                        .isInstanceOf(DomainException.class)
                        .hasFieldOrPropertyWithValue("errorCode", "TEMPLATE_NAME_EXISTS");

                verify(templateRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("Update Template Tests")
    class UpdateTemplateTests {

        @Test
        @DisplayName("Should update template successfully")
        void update_ValidRequest_Success() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            NewsletterTemplate existing = createExistingTemplate(templateId);

            UpdateTemplateRequest request = new UpdateTemplateRequest();
            request.setName("Updated Name");
            request.setSubject("Updated Subject");
            request.setBody("Updated Body");
            request.setPlaceholders("[\"new_var\"]");

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(existing));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            NewsletterTemplate result = templateService.update(templateId, request);

            // Assert
            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getSubject()).isEqualTo("Updated Subject");
            assertThat(result.getBody()).isEqualTo("Updated Body");
            assertThat(result.getPlaceholders()).isEqualTo("[\"new_var\"]");
        }

        @Test
        @DisplayName("Should throw when template not found")
        void update_NotFound_ThrowsException() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            UpdateTemplateRequest request = new UpdateTemplateRequest();
            request.setName("New Name");
            request.setSubject("New Subject");
            request.setBody("New Body");

            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> templateService.update(templateId, request))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TEMPLATE_NOT_FOUND");
        }
    }

    @Nested
    @DisplayName("Get Template Tests")
    class GetTemplateTests {

        @Test
        @DisplayName("Should get template by ID")
        void getById_Exists_ReturnsTemplate() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            NewsletterTemplate template = createExistingTemplate(templateId);

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

            // Act
            NewsletterTemplate result = templateService.getById(templateId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(templateId);
        }

        @Test
        @DisplayName("Should throw when template not found")
        void getById_NotFound_ThrowsException() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            when(templateRepository.findById(templateId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> templateService.getById(templateId))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "TEMPLATE_NOT_FOUND");
        }

        @Test
        @DisplayName("Should get all templates by gym ID")
        void getByGymId_ReturnsList() {
            // Arrange
            NewsletterTemplate t1 = createExistingTemplate(UUID.randomUUID());
            NewsletterTemplate t2 = createExistingTemplate(UUID.randomUUID());

            when(templateRepository.findByGymId(gymId)).thenReturn(List.of(t1, t2));

            // Act
            List<NewsletterTemplate> results = templateService.getByGymId(gymId);

            // Assert
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should get active templates by gym ID")
        void getActiveByGymId_ReturnsFilteredList() {
            // Arrange
            NewsletterTemplate activeTemplate = createExistingTemplate(UUID.randomUUID());

            when(templateRepository.findActiveByGymId(gymId)).thenReturn(List.of(activeTemplate));

            // Act
            List<NewsletterTemplate> results = templateService.getActiveByGymId(gymId);

            // Assert
            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Delete Template Tests")
    class DeleteTemplateTests {

        @Test
        @DisplayName("Should soft delete template")
        void delete_ExistingTemplate_SetsInactive() {
            // Arrange
            UUID templateId = UUID.randomUUID();
            NewsletterTemplate template = createExistingTemplate(templateId);
            template.setActive(true);

            when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
            when(templateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            templateService.delete(templateId);

            // Assert
            ArgumentCaptor<NewsletterTemplate> captor = ArgumentCaptor.forClass(NewsletterTemplate.class);
            verify(templateRepository).save(captor.capture());
            assertThat(captor.getValue().isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Render Template Tests")
    class RenderTemplateTests {

        @Test
        @DisplayName("Should substitute placeholders with values")
        void renderTemplate_WithVariables_SubstitutesPlaceholders() {
            // Arrange
            String body = "Hello {{first_name}} {{last_name}}! Welcome to {{gym_name}}.";
            Map<String, Object> variables = Map.of(
                    "first_name", "John",
                    "last_name", "Doe",
                    "gym_name", "Fitness Plus");

            // Act
            String result = templateService.renderTemplate(body, variables);

            // Assert
            assertThat(result).isEqualTo("Hello John Doe! Welcome to Fitness Plus.");
        }

        @Test
        @DisplayName("Should return original body when no variables")
        void renderTemplate_NoVariables_ReturnsOriginal() {
            // Arrange
            String body = "Hello {{first_name}}!";

            // Act
            String result = templateService.renderTemplate(body, null);

            // Assert
            assertThat(result).isEqualTo("Hello {{first_name}}!");
        }

        @Test
        @DisplayName("Should return original body when variables map is empty")
        void renderTemplate_EmptyVariables_ReturnsOriginal() {
            // Arrange
            String body = "Hello {{first_name}}!";

            // Act
            String result = templateService.renderTemplate(body, Map.of());

            // Assert
            assertThat(result).isEqualTo("Hello {{first_name}}!");
        }

        @Test
        @DisplayName("Should leave unmatched placeholders unchanged")
        void renderTemplate_PartialVariables_LeavesUnmatched() {
            // Arrange
            String body = "Hello {{first_name}}! Your email is {{email}}.";
            Map<String, Object> variables = Map.of("first_name", "Jane");

            // Act
            String result = templateService.renderTemplate(body, variables);

            // Assert
            assertThat(result).isEqualTo("Hello Jane! Your email is {{email}}.");
        }

        @Test
        @DisplayName("Should handle multiple occurrences of same placeholder")
        void renderTemplate_MultipleSamePlaceholder_ReplacesAll() {
            // Arrange
            String body = "Hi {{name}}! {{name}}, check your account.";
            Map<String, Object> variables = Map.of("name", "Alex");

            // Act
            String result = templateService.renderTemplate(body, variables);

            // Assert
            assertThat(result).isEqualTo("Hi Alex! Alex, check your account.");
        }
    }

    @Nested
    @DisplayName("Render Subject Tests")
    class RenderSubjectTests {

        @Test
        @DisplayName("Should render subject like template")
        void renderSubject_WithVariables_SubstitutesPlaceholders() {
            // Arrange
            String subject = "Welcome to {{gym_name}}, {{first_name}}!";
            Map<String, Object> variables = Map.of(
                    "gym_name", "PowerGym",
                    "first_name", "Mike");

            // Act
            String result = templateService.renderSubject(subject, variables);

            // Assert
            assertThat(result).isEqualTo("Welcome to PowerGym, Mike!");
        }
    }

    // Helper methods
    private NewsletterTemplate createExistingTemplate(UUID id) {
        NewsletterTemplate template = NewsletterTemplate.builder()
                .name("Existing Template")
                .subject("Existing Subject")
                .body("Existing Body")
                .templateType("EMAIL")
                .build();
        template.setId(id);
        template.setGymId(gymId);
        return template;
    }
}
