package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.gymmate.notification.internal.domain.SnsProcessedMessage;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link SnsProcessedMessage} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "SnsProcessedMessage")
@Table(name = "sns_processed_messages")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(SnsProcessedMessage.class)
public class SnsProcessedMessageJpaEntity extends BaseJpaEntity {

    @Column(name = "message_id", nullable = false, unique = true, length = 255)
    private String messageId;

    @Column(name = "topic_arn", length = 500)
    private String topicArn;

    @Column(name = "message_type", length = 100)
    private String messageType;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt = LocalDateTime.now();
}
