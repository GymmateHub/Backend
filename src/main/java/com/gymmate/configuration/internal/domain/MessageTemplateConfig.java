package com.gymmate.configuration.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Domain entity representing a configurable Email or Notification message template.
 *
 * <p>When {@code organisationId} is {@code null}, the record represents the platform-wide
 * default managed by a Super Admin. When {@code organisationId} is non-null, it represents
 * an organisation-specific override managed by a Gym Owner / Admin.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageTemplateConfig extends BaseAuditEntity {

    /** Null for platform-wide defaults; non-null for organisation-specific overrides. */
    private UUID organisationId;

    /** Canonical identifier, e.g. {@code EMAIL_PASSWORD_RESET} or {@code SUBSCRIPTION_EXPIRED}. */
    private String templateKey;

    @Builder.Default
    private MessageTemplateChannel channel = MessageTemplateChannel.EMAIL;

    /** Human-readable display name for admin UI. */
    private String name;

    /** Description of when this template is used. */
    private String description;

    /** Email subject template or Notification title template (supports {{var}} placeholders). */
    private String subjectOrTitle;

    /** Email HTML body template or Notification message template (supports {{var}} placeholders). */
    private String bodyTemplate;

    /** Optional Thymeleaf classpath template name (e.g. {@code password-reset}) used when not customised. */
    private String classpathTemplate;

    /** Comma-separated list of supported template variables (e.g. {@code firstName,otp,validityMinutes}). */
    private String availableVariables;

    /** True when an admin has explicitly customised the body away from the classpath template. */
    @Builder.Default
    private boolean bodyCustomised = false;

    public boolean isPlatformDefault() {
        return organisationId == null;
    }

    public void updateContent(String name, String description, String subjectOrTitle, String bodyTemplate,
                              Boolean bodyCustomised, Boolean active) {
        if (name != null && !name.isBlank()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (subjectOrTitle != null && !subjectOrTitle.isBlank()) {
            this.subjectOrTitle = subjectOrTitle;
        }
        if (bodyTemplate != null) {
            this.bodyTemplate = bodyTemplate;
            if (bodyCustomised != null) {
                this.bodyCustomised = bodyCustomised;
            } else if (!bodyTemplate.isBlank()) {
                this.bodyCustomised = true;
            }
        } else if (bodyCustomised != null) {
            this.bodyCustomised = bodyCustomised;
        }
        if (active != null) {
            setActive(active);
        }
    }
}

