package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.application.port.EmailSuppressionRepository;
import com.gymmate.notification.internal.domain.EmailSuppression;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link EmailSuppressionRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the EmailSuppression finders live here.
 */
@Component
@Transactional()
public class EmailSuppressionRepositoryAdapter extends JpaDomainRepositoryAdapter<EmailSuppression, UUID, EmailSuppressionJpaRepository>
        implements EmailSuppressionRepository {

    public EmailSuppressionRepositoryAdapter(EmailSuppressionJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email) {
        return this.<Optional<EmailSuppression>>fromJpa(jpaRepository.findByEmailIgnoreCaseAndActiveTrue(email));
    }

    @Override
    public List<EmailSuppression> findByEmailIgnoreCase(String email) {
        return this.<List<EmailSuppression>>fromJpa(jpaRepository.findByEmailIgnoreCase(email));
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndActiveTrue(String email) {
        return jpaRepository.existsByEmailIgnoreCaseAndActiveTrue(email);
    }
}
