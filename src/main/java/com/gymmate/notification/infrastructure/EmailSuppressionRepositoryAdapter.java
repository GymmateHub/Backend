package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.EmailSuppression;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailSuppressionRepositoryAdapter implements EmailSuppressionRepository {

    private final EmailSuppressionJpaRepository jpaRepository;

    @Override
    public EmailSuppression save(EmailSuppression suppression) {
        return jpaRepository.save(suppression);
    }

    @Override
    public Optional<EmailSuppression> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email) {
        return jpaRepository.findByEmailIgnoreCaseAndActiveTrue(email);
    }

    @Override
    public List<EmailSuppression> findByEmailIgnoreCase(String email) {
        return jpaRepository.findByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndActiveTrue(String email) {
        return jpaRepository.existsByEmailIgnoreCaseAndActiveTrue(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
