package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.EmailSuppression;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EmailSuppression domain entity.
 */
public interface EmailSuppressionRepository {

    EmailSuppression save(EmailSuppression suppression);

    Optional<EmailSuppression> findById(UUID id);

    Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email);

    List<EmailSuppression> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);

    void deleteById(UUID id);
}
