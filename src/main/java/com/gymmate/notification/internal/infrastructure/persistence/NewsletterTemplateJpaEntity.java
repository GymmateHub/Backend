package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.gymmate.notification.internal.domain.NewsletterTemplate;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link NewsletterTemplate} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "NewsletterTemplate")
@Table(name = "newsletter_templates")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(NewsletterTemplate.class)
public class NewsletterTemplateJpaEntity extends GymScopedJpaEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "template_type", length = 20)
    private String templateType = "EMAIL";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String placeholders = "[]";
}
