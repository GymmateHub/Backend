package com.gymmate.unit.notification.application;

import com.gymmate.notification.application.EmailSuppressionService;
import com.gymmate.notification.domain.EmailSuppression;
import com.gymmate.notification.domain.SuppressionReason;
import com.gymmate.notification.infrastructure.EmailSuppressionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailSuppressionService Unit Tests")
class EmailSuppressionServiceTest {

    @Mock
    private EmailSuppressionRepository suppressionRepository;

    private EmailSuppressionService suppressionService;

    @BeforeEach
    void setUp() {
        suppressionService = new EmailSuppressionService(suppressionRepository);
    }

    @Nested
    @DisplayName("isSuppressed Tests")
    class IsSuppressedTests {

        @Test
        @DisplayName("Should return false when email is null or blank")
        void shouldReturnFalseForNullOrBlank() {
            assertThat(suppressionService.isSuppressed(null)).isFalse();
            assertThat(suppressionService.isSuppressed("")).isFalse();
            assertThat(suppressionService.isSuppressed("   ")).isFalse();
            verifyNoInteractions(suppressionRepository);
        }

        @Test
        @DisplayName("Should return false when no active suppression exists")
        void shouldReturnFalseWhenNotFound() {
            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("user@example.com"))
                    .thenReturn(Optional.empty());

            assertThat(suppressionService.isSuppressed("user@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should return true when permanent suppression exists")
        void shouldReturnTrueForPermanentSuppression() {
            EmailSuppression suppression = EmailSuppression.builder()
                    .email("bounced@example.com")
                    .reason(SuppressionReason.PERMANENT_BOUNCE)
                    .bounceType("Permanent")
                    .build();

            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("bounced@example.com"))
                    .thenReturn(Optional.of(suppression));

            assertThat(suppressionService.isSuppressed("bounced@example.com")).isTrue();
        }

        @Test
        @DisplayName("Should return false for transient bounce when count is below threshold")
        void shouldReturnFalseForTransientBounceBelowThreshold() {
            EmailSuppression suppression = EmailSuppression.builder()
                    .email("soft@example.com")
                    .reason(SuppressionReason.TRANSIENT_BOUNCE)
                    .transientBounceCount(2)
                    .build();

            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("soft@example.com"))
                    .thenReturn(Optional.of(suppression));

            assertThat(suppressionService.isSuppressed("soft@example.com")).isFalse();
        }

        @Test
        @DisplayName("Should return true for transient bounce when count reaches threshold")
        void shouldReturnTrueForTransientBounceAtOrAboveThreshold() {
            EmailSuppression suppression = EmailSuppression.builder()
                    .email("soft@example.com")
                    .reason(SuppressionReason.TRANSIENT_BOUNCE)
                    .transientBounceCount(3)
                    .build();

            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("soft@example.com"))
                    .thenReturn(Optional.of(suppression));

            assertThat(suppressionService.isSuppressed("soft@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("suppressPermanent Tests")
    class SuppressPermanentTests {

        @Test
        @DisplayName("Should create new permanent suppression")
        void shouldCreateNewPermanentSuppression() {
            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("complaint@example.com"))
                    .thenReturn(Optional.empty());

            UUID orgId = UUID.randomUUID();
            suppressionService.suppressPermanent(
                    "complaint@example.com",
                    SuppressionReason.COMPLAINT,
                    "Complaint",
                    "abuse",
                    "User marked as spam",
                    orgId,
                    null);

            ArgumentCaptor<EmailSuppression> captor = ArgumentCaptor.forClass(EmailSuppression.class);
            verify(suppressionRepository).save(captor.capture());

            EmailSuppression saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo("complaint@example.com");
            assertThat(saved.getReason()).isEqualTo(SuppressionReason.COMPLAINT);
            assertThat(saved.getBounceType()).isEqualTo("Complaint");
            assertThat(saved.getOrganisationId()).isEqualTo(orgId);
            assertThat(saved.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("recordTransientBounce Tests")
    class RecordTransientBounceTests {

        @Test
        @DisplayName("Should create initial transient bounce record")
        void shouldCreateInitialTransientBounce() {
            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("transient@example.com"))
                    .thenReturn(Optional.empty());

            suppressionService.recordTransientBounce(
                    "transient@example.com",
                    "MailboxFull",
                    "Mailbox is full",
                    null,
                    null);

            ArgumentCaptor<EmailSuppression> captor = ArgumentCaptor.forClass(EmailSuppression.class);
            verify(suppressionRepository).save(captor.capture());

            EmailSuppression saved = captor.getValue();
            assertThat(saved.getEmail()).isEqualTo("transient@example.com");
            assertThat(saved.getReason()).isEqualTo(SuppressionReason.TRANSIENT_BOUNCE);
            assertThat(saved.getTransientBounceCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should escalate to permanent bounce when count reaches 3")
        void shouldEscalateToPermanentWhenThresholdReached() {
            EmailSuppression existing = EmailSuppression.builder()
                    .email("transient@example.com")
                    .reason(SuppressionReason.TRANSIENT_BOUNCE)
                    .transientBounceCount(2)
                    .build();

            when(suppressionRepository.findByEmailIgnoreCaseAndActiveTrue("transient@example.com"))
                    .thenReturn(Optional.of(existing));

            suppressionService.recordTransientBounce(
                    "transient@example.com",
                    "MailboxFull",
                    "Mailbox is full",
                    null,
                    null);

            verify(suppressionRepository).save(existing);
            assertThat(existing.getTransientBounceCount()).isEqualTo(3);
            assertThat(existing.getReason()).isEqualTo(SuppressionReason.PERMANENT_BOUNCE);
            assertThat(existing.getBounceType()).isEqualTo("Permanent");
        }
    }
}
