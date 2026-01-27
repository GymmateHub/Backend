package com.gymmate.unit.notification.domain;

import com.gymmate.notification.domain.NewsletterTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NewsletterTemplate Domain Tests")
class NewsletterTemplateDomainTest {

    @Nested
    @DisplayName("Template Creation Tests")
    class TemplateCreationTests {

        @Test
        @DisplayName("Should create template with builder and defaults")
        void createTemplate_WithBuilder_Success() {
            // Arrange & Act
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Welcome Email")
                    .subject("Welcome to {{gym_name}}!")
                    .body("<h1>Hello {{first_name}}</h1><p>Welcome to our gym!</p>")
                    .build();

            // Assert
            assertThat(template.getName()).isEqualTo("Welcome Email");
            assertThat(template.getSubject()).isEqualTo("Welcome to {{gym_name}}!");
            assertThat(template.getBody()).contains("{{first_name}}");
            assertThat(template.getTemplateType()).isEqualTo("EMAIL");
            assertThat(template.getPlaceholders()).isEqualTo("[]");
        }

        @Test
        @DisplayName("Should create template with custom type")
        void createTemplate_WithCustomType_Success() {
            // Arrange & Act
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("SMS Alert")
                    .subject("Reminder")
                    .body("Your class starts in 1 hour!")
                    .templateType("SMS")
                    .build();

            // Assert
            assertThat(template.getTemplateType()).isEqualTo("SMS");
        }

        @Test
        @DisplayName("Should create template with placeholders config")
        void createTemplate_WithPlaceholders_Success() {
            // Arrange
            String placeholders = "[\"first_name\", \"last_name\", \"gym_name\"]";

            // Act
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Template with vars")
                    .subject("Hello")
                    .body("Content")
                    .placeholders(placeholders)
                    .build();

            // Assert
            assertThat(template.getPlaceholders()).isEqualTo(placeholders);
        }
    }

    @Nested
    @DisplayName("Update Content Tests")
    class UpdateContentTests {

        @Test
        @DisplayName("Should update template name, subject, and body")
        void updateContent_AllFields_Success() {
            // Arrange
            NewsletterTemplate template = createDefaultTemplate();

            // Act
            template.updateContent("New Name", "New Subject", "New Body");

            // Assert
            assertThat(template.getName()).isEqualTo("New Name");
            assertThat(template.getSubject()).isEqualTo("New Subject");
            assertThat(template.getBody()).isEqualTo("New Body");
        }

        @Test
        @DisplayName("Should preserve template type when updating content")
        void updateContent_PreservesType_Success() {
            // Arrange
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Original")
                    .subject("Original")
                    .body("Original")
                    .templateType("SMS")
                    .build();

            // Act
            template.updateContent("Updated", "Updated", "Updated");

            // Assert
            assertThat(template.getTemplateType()).isEqualTo("SMS");
        }

        @Test
        @DisplayName("Should preserve placeholders when updating content")
        void updateContent_PreservesPlaceholders_Success() {
            // Arrange
            String placeholders = "[\"first_name\"]";
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Original")
                    .subject("Original")
                    .body("Original")
                    .placeholders(placeholders)
                    .build();

            // Act
            template.updateContent("Updated", "Updated", "Updated");

            // Assert
            assertThat(template.getPlaceholders()).isEqualTo(placeholders);
        }
    }

    @Nested
    @DisplayName("Update Placeholders Tests")
    class UpdatePlaceholdersTests {

        @Test
        @DisplayName("Should update placeholders configuration")
        void updatePlaceholders_ValidJson_Success() {
            // Arrange
            NewsletterTemplate template = createDefaultTemplate();
            String newPlaceholders = "[\"member_name\", \"class_name\", \"date\"]";

            // Act
            template.updatePlaceholders(newPlaceholders);

            // Assert
            assertThat(template.getPlaceholders()).isEqualTo(newPlaceholders);
        }

        @Test
        @DisplayName("Should preserve content when updating placeholders")
        void updatePlaceholders_PreservesContent_Success() {
            // Arrange
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Test Template")
                    .subject("Test Subject")
                    .body("Test Body")
                    .build();

            // Act
            template.updatePlaceholders("[\"test\"]");

            // Assert
            assertThat(template.getName()).isEqualTo("Test Template");
            assertThat(template.getSubject()).isEqualTo("Test Subject");
            assertThat(template.getBody()).isEqualTo("Test Body");
        }

        @Test
        @DisplayName("Should allow empty placeholders array")
        void updatePlaceholders_EmptyArray_Success() {
            // Arrange
            NewsletterTemplate template = NewsletterTemplate.builder()
                    .name("Test")
                    .subject("Test")
                    .body("Test")
                    .placeholders("[\"old\"]")
                    .build();

            // Act
            template.updatePlaceholders("[]");

            // Assert
            assertThat(template.getPlaceholders()).isEqualTo("[]");
        }
    }

    // Helper methods
    private NewsletterTemplate createDefaultTemplate() {
        return NewsletterTemplate.builder()
                .name("Default Template")
                .subject("Default Subject")
                .body("<p>Default content</p>")
                .build();
    }
}
