package com.gymmate.notification.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Domain entity representing a reusable newsletter/message template.
 * Templates can be used to create campaigns for sending bulk emails.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "newsletter_templates")
public class NewsletterTemplate extends GymScopedEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "template_type", length = 20)
    @Builder.Default
    private String templateType = "EMAIL";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
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
