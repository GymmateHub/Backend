package com.gymmate.notification.domain;

import com.gymmate.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Domain entity for tracking processed SNS messages to guarantee idempotent webhook processing.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "sns_processed_messages")
public class SnsProcessedMessage extends BaseEntity {

    @Column(name = "message_id", nullable = false, unique = true, length = 255)
    private String messageId;

    @Column(name = "topic_arn", length = 500)
    private String topicArn;

    @Column(name = "message_type", length = 100)
    private String messageType;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    @Builder.Default
    private LocalDateTime processedAt = LocalDateTime.now();
}
