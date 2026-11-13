package com.gymmate.configuration.internal.infrastructure.persistence;

import com.gymmate.configuration.internal.domain.MessageTemplateChannel;
import com.gymmate.configuration.internal.domain.MessageTemplateConfig;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import com.gymmate.shared.infrastructure.persistence.DomainModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Persistence entity for {@link MessageTemplateConfig}.
 *
 * <p>Extends {@link BaseAuditJpaEntity} directly rather than {@code TenantJpaEntity} so that
 * platform-wide default rows ({@code organisation_id IS NULL}) are not hidden when the
 * Hibernate tenant filter is enabled.
 */
@Entity(name = "MessageTemplateConfig")
@Table(name = "message_template_configs")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MessageTemplateConfig.class)
public class MessageTemplateConfigJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "template_key", nullable = false, length = 100)
    private String templateKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 30)
    private MessageTemplateChannel channel = MessageTemplateChannel.EMAIL;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "subject_or_title", nullable = false, length = 300)
    private String subjectOrTitle;

    @Column(name = "body_template", columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "classpath_template", length = 100)
    private String classpathTemplate;

    @Column(name = "available_variables", length = 500)
    private String availableVariables;

    @Column(name = "body_customised", nullable = false)
    private boolean bodyCustomised = false;
}

