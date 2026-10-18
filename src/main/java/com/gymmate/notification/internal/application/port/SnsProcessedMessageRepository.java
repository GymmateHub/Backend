package com.gymmate.notification.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.notification.internal.domain.SnsProcessedMessage;

import java.util.Optional;
import java.util.UUID;

public interface SnsProcessedMessageRepository extends DomainRepository<SnsProcessedMessage, UUID> {

    Optional<SnsProcessedMessage> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);
}
