package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.identity.internal.domain.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.PasswordResetTokenRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PasswordResetTokenRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class PasswordResetTokenRepositoryAdapter extends DomainRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return this.<Optional<PasswordResetToken>>fromJpa(jpaRepository.findByToken(token));
    }

    @Override
    public void deleteByUser_Id(UUID userId) {
        jpaRepository.deleteByUser_Id(userId);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<PasswordResetToken> saveAll(Iterable<PasswordResetToken> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<PasswordResetToken> findById(UUID id) {
        return this.<Optional<PasswordResetToken>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<PasswordResetToken> findAll() {
        return this.<List<PasswordResetToken>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<PasswordResetToken> findAllById(Iterable<UUID> ids) {
        return this.<List<PasswordResetToken>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(PasswordResetToken entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<PasswordResetToken> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public PasswordResetToken saveAndFlush(PasswordResetToken entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<PasswordResetToken> findAll(Pageable pageable) {
        return this.<Page<PasswordResetToken>>fromJpa(jpaRepository.findAll(pageable));
    }
}
