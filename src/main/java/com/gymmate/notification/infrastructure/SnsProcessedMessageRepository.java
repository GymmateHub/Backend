package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.SnsProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnsProcessedMessageRepository extends JpaRepository<SnsProcessedMessage, UUID> {

    Optional<SnsProcessedMessage> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);
}
