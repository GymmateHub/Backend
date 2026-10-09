package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.domain.SnsProcessedMessage;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.notification.internal.application.port.SnsProcessedMessageRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SnsProcessedMessageRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the SnsProcessedMessage finders live here.
 */
@Component()
@Transactional()
public class SnsProcessedMessageRepositoryAdapter extends JpaDomainRepositoryAdapter<SnsProcessedMessage, UUID, SnsProcessedMessageJpaRepository>
        implements SnsProcessedMessageRepository {

    public SnsProcessedMessageRepositoryAdapter(SnsProcessedMessageJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<SnsProcessedMessage> findByMessageId(String messageId) {
        return this.<Optional<SnsProcessedMessage>>fromJpa(jpaRepository.findByMessageId(messageId));
    }

    @Override
    public boolean existsByMessageId(String messageId) {
        return jpaRepository.existsByMessageId(messageId);
    }
}
