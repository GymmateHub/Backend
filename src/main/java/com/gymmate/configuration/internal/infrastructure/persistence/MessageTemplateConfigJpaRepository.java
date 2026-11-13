package com.gymmate.configuration.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageTemplateConfigJpaRepository extends JpaRepository<MessageTemplateConfigJpaEntity, UUID> {

    List<MessageTemplateConfigJpaEntity> findByOrganisationIdIsNullOrderByTemplateKeyAsc();

    List<MessageTemplateConfigJpaEntity> findByOrganisationIdOrderByTemplateKeyAsc(UUID organisationId);

    Optional<MessageTemplateConfigJpaEntity> findByTemplateKeyAndOrganisationIdIsNull(String templateKey);

    Optional<MessageTemplateConfigJpaEntity> findByTemplateKeyAndOrganisationId(String templateKey, UUID organisationId);
}

