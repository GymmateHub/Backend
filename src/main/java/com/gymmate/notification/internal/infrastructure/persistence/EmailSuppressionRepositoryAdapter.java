package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.application.port.EmailSuppressionRepository;
import com.gymmate.notification.internal.domain.EmailSuppression;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link EmailSuppressionRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class EmailSuppressionRepositoryAdapter extends DomainRepositoryAdapter implements EmailSuppressionRepository {

    private final EmailSuppressionJpaRepository jpaRepository;

    public EmailSuppressionRepositoryAdapter(EmailSuppressionJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EmailSuppression save(EmailSuppression suppression) {
        return save(jpaRepository, suppression);
    }

    @Override
    public Optional<EmailSuppression> findById(UUID id) {
        return this.<Optional<EmailSuppression>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email) {
        return this.<Optional<EmailSuppression>>fromJpa(jpaRepository.findByEmailIgnoreCaseAndActiveTrue(email));
    }

    @Override
    public List<EmailSuppression> findByEmailIgnoreCase(String email) {
        return this.<List<EmailSuppression>>fromJpa(jpaRepository.findByEmailIgnoreCase(email));
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndActiveTrue(String email) {
        return jpaRepository.existsByEmailIgnoreCaseAndActiveTrue(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<EmailSuppression> saveAll(Iterable<EmailSuppression> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<EmailSuppression> findAll() {
        return this.<List<EmailSuppression>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<EmailSuppression> findAllById(Iterable<UUID> ids) {
        return this.<List<EmailSuppression>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void delete(EmailSuppression entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<EmailSuppression> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public EmailSuppression saveAndFlush(EmailSuppression entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<EmailSuppression> findAll(Pageable pageable) {
        return this.<Page<EmailSuppression>>fromJpa(jpaRepository.findAll(pageable));
    }
}
