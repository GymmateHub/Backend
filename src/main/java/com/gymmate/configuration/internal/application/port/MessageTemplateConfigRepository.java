package com.gymmate.configuration.internal.application.port;

import com.gymmate.configuration.internal.domain.MessageTemplateConfig;
import com.gymmate.shared.application.port.DomainRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application port for persisting and querying {@link MessageTemplateConfig} records.
 */
public interface MessageTemplateConfigRepository extends DomainRepository<MessageTemplateConfig, UUID> {

    List<MessageTemplateConfig> findByOrganisationIdIsNull();

    List<MessageTemplateConfig> findByOrganisationId(UUID organisationId);

    Optional<MessageTemplateConfig> findByTemplateKeyAndOrganisationIdIsNull(String templateKey);

    Optional<MessageTemplateConfig> findByTemplateKeyAndOrganisationId(String templateKey, UUID organisationId);
}

