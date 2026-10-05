package com.gymmate.notification.internal.domain;

import com.gymmate.shared.domain.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Domain entity for tracking processed SNS messages to guarantee idempotent webhook processing.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SnsProcessedMessage extends BaseEntity {

    private String messageId;

    private String topicArn;

    private String messageType;

    private String eventType;

    @Builder.Default
    private 
    LocalDateTime processedAt = LocalDateTime.now();
}
