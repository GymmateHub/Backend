package com.gymmate.configuration.internal.infrastructure.persistence;

import com.gymmate.configuration.internal.application.port.MessageTemplateConfigRepository;
import com.gymmate.configuration.internal.domain.MessageTemplateConfig;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence adapter implementing {@link MessageTemplateConfigRepository} with Spring Data JPA.
 */
@Component
@Transactional
public class MessageTemplateConfigRepositoryAdapter
        extends JpaDomainRepositoryAdapter<MessageTemplateConfig, UUID, MessageTemplateConfigJpaRepository>
        implements MessageTemplateConfigRepository {

    public MessageTemplateConfigRepositoryAdapter(
            MessageTemplateConfigJpaRepository jpaRepository,
            DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<MessageTemplateConfig> findByOrganisationIdIsNull() {
        return this.fromJpa(jpaRepository.findByOrganisationIdIsNullOrderByTemplateKeyAsc());
    }

    @Override
    public List<MessageTemplateConfig> findByOrganisationId(UUID organisationId) {
        return this.fromJpa(jpaRepository.findByOrganisationIdOrderByTemplateKeyAsc(organisationId));
    }

    @Override
    public Optional<MessageTemplateConfig> findByTemplateKeyAndOrganisationIdIsNull(String templateKey) {
        return this.fromJpa(jpaRepository.findByTemplateKeyAndOrganisationIdIsNull(templateKey));
    }

    @Override
    public Optional<MessageTemplateConfig> findByTemplateKeyAndOrganisationId(String templateKey, UUID organisationId) {
        return this.fromJpa(jpaRepository.findByTemplateKeyAndOrganisationId(templateKey, organisationId));
    }
}

