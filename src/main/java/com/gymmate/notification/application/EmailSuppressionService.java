package com.gymmate.notification.application;

import com.gymmate.notification.domain.EmailSuppression;
import com.gymmate.notification.domain.SuppressionReason;
import com.gymmate.notification.infrastructure.EmailSuppressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service managing email suppressions (hard bounces, soft bounce escalation, complaints, unsubscribes).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailSuppressionService {

    private final EmailSuppressionRepository suppressionRepository;

    /**
     * Threshold of consecutive transient bounces before escalating to permanent suppression.
     */
    private static final int TRANSIENT_BOUNCE_THRESHOLD = 3;

    /**
     * Check if an email address is actively suppressed.
     */
    @Transactional(readOnly = true)
    public boolean isSuppressed(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return suppressionRepository.findByEmailIgnoreCaseAndActiveTrue(email.trim())
                .map(s -> {
                    if (s.getReason() == SuppressionReason.TRANSIENT_BOUNCE) {
                        return s.getTransientBounceCount() >= TRANSIENT_BOUNCE_THRESHOLD;
                    }
                    return true;
                })
                .orElse(false);
    }

    /**
     * Suppress an email address permanently (hard bounce, complaint, unsubscribe).
     */
    @Transactional
    public void suppressPermanent(String email, SuppressionReason reason, String bounceType, String bounceSubType, String diagnosticCode, UUID organisationId, UUID gymId) {
        if (email == null || email.isBlank()) return;
        String normalizedEmail = email.trim().toLowerCase();

        Optional<EmailSuppression> existing = suppressionRepository.findByEmailIgnoreCaseAndActiveTrue(normalizedEmail);
        if (existing.isPresent()) {
            EmailSuppression suppression = existing.get();
            suppression.setReason(reason);
            suppression.setBounceType(bounceType);
            suppression.setBounceSubType(bounceSubType);
            suppression.setDiagnosticCode(diagnosticCode);
            suppression.setActive(true);
            suppressionRepository.save(suppression);
            log.warn("Updated permanent email suppression for {} (reason: {})", normalizedEmail, reason);
        } else {
            EmailSuppression suppression = EmailSuppression.builder()
                    .email(normalizedEmail)
                    .reason(reason)
                    .bounceType(bounceType)
                    .bounceSubType(bounceSubType)
                    .diagnosticCode(diagnosticCode)
                    .transientBounceCount(1)
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .build();
            suppressionRepository.save(suppression);
            log.warn("Created permanent email suppression for {} (reason: {})", normalizedEmail, reason);
        }
    }

    /**
     * Record a transient (soft) bounce. If count reaches threshold, escalate to permanent.
     */
    @Transactional
    public void recordTransientBounce(String email, String bounceSubType, String diagnosticCode, UUID organisationId, UUID gymId) {
        if (email == null || email.isBlank()) return;
        String normalizedEmail = email.trim().toLowerCase();

        Optional<EmailSuppression> existing = suppressionRepository.findByEmailIgnoreCaseAndActiveTrue(normalizedEmail);
        if (existing.isPresent()) {
            EmailSuppression suppression = existing.get();
            suppression.incrementTransientBounce(bounceSubType, diagnosticCode);
            if (suppression.getTransientBounceCount() >= TRANSIENT_BOUNCE_THRESHOLD) {
                suppression.escalateToPermanent(diagnosticCode);
                log.error("Transient bounce threshold ({}) reached for {}. Escalated to permanent suppression.",
                        TRANSIENT_BOUNCE_THRESHOLD, normalizedEmail);
            } else {
                log.warn("Incremented transient bounce count for {} ({}/{})",
                        normalizedEmail, suppression.getTransientBounceCount(), TRANSIENT_BOUNCE_THRESHOLD);
            }
            suppressionRepository.save(suppression);
        } else {
            EmailSuppression suppression = EmailSuppression.builder()
                    .email(normalizedEmail)
                    .reason(SuppressionReason.TRANSIENT_BOUNCE)
                    .bounceType("Transient")
                    .bounceSubType(bounceSubType)
                    .diagnosticCode(diagnosticCode)
                    .transientBounceCount(1)
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .build();
            suppressionRepository.save(suppression);
            log.warn("Recorded initial transient bounce for {}", normalizedEmail);
        }
    }

    /**
     * Unsuppress/remove suppression for an email address (e.g. manual override).
     */
    @Transactional
    public void unsuppress(String email) {
        if (email == null || email.isBlank()) return;
        String normalizedEmail = email.trim().toLowerCase();
        suppressionRepository.findByEmailIgnoreCaseAndActiveTrue(normalizedEmail).ifPresent(s -> {
            s.deactivate();
            suppressionRepository.save(s);
            log.info("Deactivated email suppression for {}", normalizedEmail);
        });
    }
}
