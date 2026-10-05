package com.gymmate.notification.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnsProcessedMessageJpaRepository extends JpaRepository<SnsProcessedMessageJpaEntity, UUID> {

    Optional<SnsProcessedMessageJpaEntity> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);
}
