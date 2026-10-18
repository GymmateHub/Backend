package com.gymmate.notification.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.notification.internal.domain.EmailSuppression;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for EmailSuppression domain entity.
 */
public interface EmailSuppressionRepository extends DomainRepository<EmailSuppression, UUID> {

    Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email);

    List<EmailSuppression> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);
}
