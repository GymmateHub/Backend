package com.gymmate.notification.application;

import com.gymmate.notification.api.dto.CreateTemplateRequest;
import com.gymmate.notification.api.dto.UpdateTemplateRequest;
import com.gymmate.notification.domain.NewsletterTemplate;
import com.gymmate.notification.infrastructure.NewsletterTemplateRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing newsletter templates.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NewsletterTemplateService {

    private final NewsletterTemplateRepository templateRepository;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    /**
     * Create a new newsletter template.
     */
    @Transactional
    public NewsletterTemplate create(CreateTemplateRequest request, UUID createdBy) {
        UUID gymId = request.getGymId();
        if (gymId == null) {
            gymId = TenantContext.getCurrentGymId();
        }

        if (templateRepository.existsByGymIdAndName(gymId, request.getName())) {
            throw new DomainException("TEMPLATE_NAME_EXISTS",
                    "A template with this name already exists");
        }

        NewsletterTemplate template = NewsletterTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .body(request.getBody())
                .templateType(request.getTemplateType())
                .placeholders(request.getPlaceholders())
                .build();
        template.setCreatedBy(createdBy.toString());

        template.setGymId(gymId);
        template.setOrganisationId(TenantContext.getCurrentTenantId());

        NewsletterTemplate saved = templateRepository.save(template);
        log.info("Created newsletter template: {} for gym: {}", saved.getId(), gymId);
        return saved;
    }

    /**
     * Update an existing template.
     */
    @Transactional
    public NewsletterTemplate update(UUID id, UpdateTemplateRequest request) {
        NewsletterTemplate template = getById(id);

        template.updateContent(
                request.getName(),
                request.getSubject(),
                request.getBody());

        if (request.getPlaceholders() != null) {
            template.updatePlaceholders(request.getPlaceholders());
        }

        NewsletterTemplate updated = templateRepository.save(template);
        log.info("Updated newsletter template: {}", id);
        return updated;
    }

    /**
     * Get template by ID.
     */
    @Transactional(readOnly = true)
    public NewsletterTemplate getById(UUID id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new DomainException("TEMPLATE_NOT_FOUND",
                        "Newsletter template not found: " + id));
    }

    /**
     * Get all templates for a gym.
     */
    @Transactional(readOnly = true)
    public List<NewsletterTemplate> getByGymId(UUID gymId) {
        return templateRepository.findByGymId(gymId);
    }

    /**
     * Get active templates for a gym.
     */
    @Transactional(readOnly = true)
    public List<NewsletterTemplate> getActiveByGymId(UUID gymId) {
        return templateRepository.findActiveByGymId(gymId);
    }

    /**
     * Soft delete a template.
     */
    @Transactional
    public void delete(UUID id) {
        NewsletterTemplate template = getById(id);
        template.setActive(false);
        templateRepository.save(template);
        log.info("Soft-deleted newsletter template: {}", id);
    }

    /**
     * Render a template with variable substitution.
     */
    public String renderTemplate(String body, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return body;
        }

        String result = body;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(body);

        while (matcher.find()) {
            String placeholder = matcher.group(0);
            String key = matcher.group(1);

            Object value = variables.get(key);
            if (value != null) {
                result = result.replace(placeholder, value.toString());
            }
        }

        return result;
    }

    /**
     * Render subject with variable substitution.
     */
    public String renderSubject(String subject, Map<String, Object> variables) {
        return renderTemplate(subject, variables);
    }
}
