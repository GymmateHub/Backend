package com.gymmate.notification.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailSuppressionJpaRepository extends JpaRepository<EmailSuppressionJpaEntity, UUID> {

    Optional<EmailSuppressionJpaEntity> findByEmailIgnoreCaseAndActiveTrue(String email);

    List<EmailSuppressionJpaEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);
}
