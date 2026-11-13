package com.gymmate.configuration.internal.application;

import com.gymmate.configuration.api.MessageTemplateConfigApi;
import com.gymmate.configuration.api.dto.RenderedEmail;
import com.gymmate.configuration.api.dto.RenderedNotification;
import com.gymmate.configuration.internal.application.dto.MessageTemplateConfigResponse;
import com.gymmate.configuration.internal.application.dto.UpdateMessageTemplateRequest;
import com.gymmate.configuration.internal.application.port.MessageTemplateConfigRepository;
import com.gymmate.configuration.internal.domain.DefaultMessageTemplates;
import com.gymmate.configuration.internal.domain.MessageTemplateChannel;
import com.gymmate.configuration.internal.domain.MessageTemplateConfig;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service managing platform-wide and organisation-scoped email and notification
 * message templates, and rendering templates for outbound emails and notifications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageTemplateConfigService implements MessageTemplateConfigApi {

    private final MessageTemplateConfigRepository repository;

    @Override
    public RenderedEmail renderEmail(String templateKey, UUID organisationId, Map<String, Object> variables) {
        ResolvedTemplate resolved = resolveEffective(templateKey, organisationId);
        if (resolved == null) {
            throw new ResourceNotFoundException("MessageTemplateConfig", templateKey);
        }
        MessageTemplateConfig config = resolved.config();
        Map<String, Object> safeVars = variables != null ? variables : Map.of();
        String subject = DefaultMessageTemplates.interpolate(config.getSubjectOrTitle(), safeVars);
        String body = DefaultMessageTemplates.interpolate(config.getBodyTemplate(), safeVars);

        return new RenderedEmail(
                config.getTemplateKey(),
                subject,
                body,
                config.getClasspathTemplate(),
                config.isBodyCustomised(),
                config.isActive()
        );
    }

    @Override
    public RenderedNotification renderNotification(
            String templateKey,
            UUID organisationId,
            Map<String, Object> variables,
            String defaultTitle,
            String defaultMessage) {
        Map<String, Object> safeVars = variables != null ? variables : Map.of();
        ResolvedTemplate resolved = resolveEffective(templateKey, organisationId);
        if (resolved == null) {
            return new RenderedNotification(
                    templateKey,
                    DefaultMessageTemplates.interpolate(defaultTitle, safeVars),
                    DefaultMessageTemplates.interpolate(defaultMessage, safeVars),
                    true
            );
        }
        MessageTemplateConfig config = resolved.config();
        String title = config.getSubjectOrTitle() != null && !config.getSubjectOrTitle().isBlank()
                ? DefaultMessageTemplates.interpolate(config.getSubjectOrTitle(), safeVars)
                : DefaultMessageTemplates.interpolate(defaultTitle, safeVars);
        String message = config.getBodyTemplate() != null && !config.getBodyTemplate().isBlank()
                ? DefaultMessageTemplates.interpolate(config.getBodyTemplate(), safeVars)
                : DefaultMessageTemplates.interpolate(defaultMessage, safeVars);

        return new RenderedNotification(
                config.getTemplateKey(),
                title,
                message,
                config.isActive()
        );
    }

    /**
     * Lists all platform-wide default templates (merging database platform records with the built-in catalog).
     */
    public List<MessageTemplateConfigResponse> listPlatformTemplates() {
        Map<String, MessageTemplateConfig> byKey = new LinkedHashMap<>();
        for (MessageTemplateConfig builtIn : DefaultMessageTemplates.all()) {
            byKey.put(builtIn.getTemplateKey(), builtIn);
        }
        for (MessageTemplateConfig dbRecord : repository.findByOrganisationIdIsNull()) {
            byKey.put(dbRecord.getTemplateKey(), dbRecord);
        }
        return byKey.values().stream()
                .map(c -> MessageTemplateConfigResponse.from(c, false))
                .toList();
    }

    /**
     * Gets a single platform-wide default template by key.
     */
    public MessageTemplateConfigResponse getPlatformTemplate(String templateKey) {
        MessageTemplateConfig config = repository.findByTemplateKeyAndOrganisationIdIsNull(templateKey)
                .or(() -> DefaultMessageTemplates.findByKey(templateKey))
                .orElseThrow(() -> new ResourceNotFoundException("MessageTemplateConfig", templateKey));
        return MessageTemplateConfigResponse.from(config, false);
    }

    /**
     * Updates (or creates) a platform-wide default template.
     */
    @Transactional
    public MessageTemplateConfigResponse updatePlatformTemplate(String templateKey, UpdateMessageTemplateRequest request) {
        MessageTemplateConfig config = repository.findByTemplateKeyAndOrganisationIdIsNull(templateKey)
                .orElseGet(() -> DefaultMessageTemplates.findByKey(templateKey)
                        .orElseGet(() -> MessageTemplateConfig.builder()
                                .templateKey(templateKey)
                                .channel(inferChannel(templateKey))
                                .name(request.name() != null ? request.name() : templateKey)
                                .build()));

        config.setOrganisationId(null);
        config.updateContent(
                request.name(),
                request.description(),
                request.subjectOrTitle(),
                request.bodyTemplate(),
                request.bodyCustomised(),
                request.active()
        );

        MessageTemplateConfig saved = repository.save(config);
        log.info("Updated platform message template configuration for key={}", templateKey);
        return MessageTemplateConfigResponse.from(saved, false);
    }

    /**
     * Resets a platform-wide template back to its built-in factory default.
     */
    @Transactional
    public MessageTemplateConfigResponse resetPlatformTemplate(String templateKey) {
        MessageTemplateConfig builtIn = DefaultMessageTemplates.findByKey(templateKey)
                .orElseThrow(() -> new ResourceNotFoundException("DefaultMessageTemplate", templateKey));

        MessageTemplateConfig target = repository.findByTemplateKeyAndOrganisationIdIsNull(templateKey)
                .map(existing -> {
                    existing.setName(builtIn.getName());
                    existing.setDescription(builtIn.getDescription());
                    existing.setSubjectOrTitle(builtIn.getSubjectOrTitle());
                    existing.setBodyTemplate(builtIn.getBodyTemplate());
                    existing.setClasspathTemplate(builtIn.getClasspathTemplate());
                    existing.setAvailableVariables(builtIn.getAvailableVariables());
                    existing.setBodyCustomised(builtIn.isBodyCustomised());
                    existing.setActive(true);
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(builtIn));

        log.info("Reset platform message template key={} to factory default", templateKey);
        return MessageTemplateConfigResponse.from(target, false);
    }

    /**
     * Lists effective templates for the given organisation (organisation override when present, otherwise platform default).
     */
    public List<MessageTemplateConfigResponse> listOrganisationTemplates(UUID organisationId) {
        Map<String, MessageTemplateConfig> platformByKey = new LinkedHashMap<>();
        for (MessageTemplateConfig builtIn : DefaultMessageTemplates.all()) {
            platformByKey.put(builtIn.getTemplateKey(), builtIn);
        }
        for (MessageTemplateConfig dbPlatform : repository.findByOrganisationIdIsNull()) {
            platformByKey.put(dbPlatform.getTemplateKey(), dbPlatform);
        }

        Map<String, MessageTemplateConfig> orgOverrides = new LinkedHashMap<>();
        if (organisationId != null) {
            for (MessageTemplateConfig override : repository.findByOrganisationId(organisationId)) {
                orgOverrides.put(override.getTemplateKey(), override);
                platformByKey.putIfAbsent(override.getTemplateKey(), override);
            }
        }

        List<MessageTemplateConfigResponse> responses = new ArrayList<>(platformByKey.size());
        for (String key : platformByKey.keySet()) {
            MessageTemplateConfig override = orgOverrides.get(key);
            if (override != null) {
                responses.add(MessageTemplateConfigResponse.from(override, true));
            } else {
                responses.add(MessageTemplateConfigResponse.from(platformByKey.get(key), false));
            }
        }
        return responses;
    }

    /**
     * Gets the effective template for the given organisation and template key.
     */
    public MessageTemplateConfigResponse getOrganisationTemplate(UUID organisationId, String templateKey) {
        ResolvedTemplate resolved = resolveEffective(templateKey, organisationId);
        if (resolved == null) {
            throw new ResourceNotFoundException("MessageTemplateConfig", templateKey);
        }
        return MessageTemplateConfigResponse.from(resolved.config(), resolved.organisationOverride());
    }

    /**
     * Creates or updates an organisation-specific template override.
     */
    @Transactional
    public MessageTemplateConfigResponse upsertOrganisationOverride(
            UUID organisationId, String templateKey, UpdateMessageTemplateRequest request) {
        if (organisationId == null) {
            throw new IllegalArgumentException("Organisation ID is required for tenant template overrides");
        }

        MessageTemplateConfig base = repository.findByTemplateKeyAndOrganisationIdIsNull(templateKey)
                .or(() -> DefaultMessageTemplates.findByKey(templateKey))
                .orElseThrow(() -> new ResourceNotFoundException("MessageTemplateConfig", templateKey));

        MessageTemplateConfig override = repository.findByTemplateKeyAndOrganisationId(templateKey, organisationId)
                .orElseGet(() -> {
                    MessageTemplateConfig copy = DefaultMessageTemplates.copyOf(base);
                    copy.setOrganisationId(organisationId);
                    return copy;
                });

        override.updateContent(
                request.name(),
                request.description(),
                request.subjectOrTitle(),
                request.bodyTemplate(),
                request.bodyCustomised(),
                request.active()
        );

        MessageTemplateConfig saved = repository.save(override);
        log.info("Upserted organisation={} override for message template key={}", organisationId, templateKey);
        return MessageTemplateConfigResponse.from(saved, true);
    }

    /**
     * Removes an organisation's custom override for a template key so it reverts to the platform default.
     */
    @Transactional
    public MessageTemplateConfigResponse deleteOrganisationOverride(UUID organisationId, String templateKey) {
        if (organisationId != null) {
            repository.findByTemplateKeyAndOrganisationId(templateKey, organisationId)
                    .ifPresent(override -> {
                        repository.delete(override);
                        log.info("Deleted organisation={} override for message template key={}", organisationId, templateKey);
                    });
        }
        return getPlatformTemplate(templateKey);
    }

    private ResolvedTemplate resolveEffective(String templateKey, UUID organisationId) {
        if (templateKey == null || templateKey.isBlank()) {
            return null;
        }
        if (organisationId != null) {
            Optional<MessageTemplateConfig> orgOverride =
                    repository.findByTemplateKeyAndOrganisationId(templateKey, organisationId);
            if (orgOverride.isPresent()) {
                return new ResolvedTemplate(orgOverride.get(), true);
            }
        }
        Optional<MessageTemplateConfig> platformDefault =
                repository.findByTemplateKeyAndOrganisationIdIsNull(templateKey);
        if (platformDefault.isPresent()) {
            return new ResolvedTemplate(platformDefault.get(), false);
        }
        return DefaultMessageTemplates.findByKey(templateKey)
                .map(c -> new ResolvedTemplate(c, false))
                .orElse(null);
    }

    private static MessageTemplateChannel inferChannel(String templateKey) {
        return templateKey != null && templateKey.startsWith("EMAIL_")
                ? MessageTemplateChannel.EMAIL
                : MessageTemplateChannel.NOTIFICATION;
    }

    private record ResolvedTemplate(MessageTemplateConfig config, boolean organisationOverride) {
    }
}

