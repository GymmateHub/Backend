package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.domain.SnsProcessedMessage;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.notification.internal.application.port.SnsProcessedMessageRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SnsProcessedMessageRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SnsProcessedMessageRepositoryAdapter extends DomainRepositoryAdapter implements SnsProcessedMessageRepository {

    private final SnsProcessedMessageJpaRepository jpaRepository;

    public SnsProcessedMessageRepositoryAdapter(SnsProcessedMessageJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<SnsProcessedMessage> findByMessageId(String messageId) {
        return this.<Optional<SnsProcessedMessage>>fromJpa(jpaRepository.findByMessageId(messageId));
    }

    @Override
    public boolean existsByMessageId(String messageId) {
        return jpaRepository.existsByMessageId(messageId);
    }

    @Override
    public SnsProcessedMessage save(SnsProcessedMessage entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<SnsProcessedMessage> saveAll(Iterable<SnsProcessedMessage> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<SnsProcessedMessage> findById(UUID id) {
        return this.<Optional<SnsProcessedMessage>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<SnsProcessedMessage> findAll() {
        return this.<List<SnsProcessedMessage>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<SnsProcessedMessage> findAllById(Iterable<UUID> ids) {
        return this.<List<SnsProcessedMessage>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(SnsProcessedMessage entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<SnsProcessedMessage> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public SnsProcessedMessage saveAndFlush(SnsProcessedMessage entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<SnsProcessedMessage> findAll(Pageable pageable) {
        return this.<Page<SnsProcessedMessage>>fromJpa(jpaRepository.findAll(pageable));
    }
}
