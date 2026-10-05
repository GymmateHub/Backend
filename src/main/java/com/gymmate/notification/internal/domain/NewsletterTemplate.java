package com.gymmate.notification.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;
/**
 * Domain entity representing a reusable newsletter/message template.
 * Templates can be used to create campaigns for sending bulk emails.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class NewsletterTemplate extends GymScopedEntity {

    private String name;

    private String subject;

    private String body;

    @Builder.Default
    private 
    String templateType = "EMAIL";

    @Builder.Default
    private String placeholders = "[]";

    /**
     * Update template content.
     */
    public void updateContent(String name, String subject, String body) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    /**
     * Update placeholders configuration.
     */
    public void updatePlaceholders(String placeholders) {
        this.placeholders = placeholders;
    }
}
