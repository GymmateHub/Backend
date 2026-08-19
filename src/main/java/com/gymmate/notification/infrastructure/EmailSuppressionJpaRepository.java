package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.EmailSuppression;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailSuppressionJpaRepository extends JpaRepository<EmailSuppression, UUID> {

    Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email);

    List<EmailSuppression> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);
}
