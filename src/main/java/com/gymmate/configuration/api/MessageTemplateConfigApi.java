package com.gymmate.configuration.api;

import com.gymmate.configuration.api.dto.RenderedEmail;
import com.gymmate.configuration.api.dto.RenderedNotification;

import java.util.Map;
import java.util.UUID;

/**
 * Public facade for resolving and rendering configurable transactional email templates
 * and system/in-app notification messages.
 *
 * <p>Resolution order for any {@code templateKey}:
 * <ol>
 *   <li>Organisation-level override (when {@code organisationId} is provided and an override exists)</li>
 *   <li>Platform-wide default stored in the database ({@code organisation_id IS NULL})</li>
 *   <li>Built-in code/classpath default</li>
 * </ol>
 */
public interface MessageTemplateConfigApi {

    /**
     * Resolves and renders an email template for the given key and optional organisation context.
     */
    RenderedEmail renderEmail(String templateKey, UUID organisationId, Map<String, Object> variables);

    /**
     * Resolves and renders a notification message for the given key/eventType and optional organisation context,
     * falling back to {@code defaultTitle} and {@code defaultMessage} if no template is registered for the key.
     */
    RenderedNotification renderNotification(
            String templateKey,
            UUID organisationId,
            Map<String, Object> variables,
            String defaultTitle,
            String defaultMessage);
}

