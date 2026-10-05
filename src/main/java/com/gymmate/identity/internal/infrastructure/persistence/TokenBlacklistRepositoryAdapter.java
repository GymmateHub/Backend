package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.identity.internal.domain.TokenBlacklist;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.TokenBlacklistRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link TokenBlacklistRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class TokenBlacklistRepositoryAdapter extends DomainRepositoryAdapter implements TokenBlacklistRepository {

    private final TokenBlacklistJpaRepository jpaRepository;

    public TokenBlacklistRepositoryAdapter(TokenBlacklistJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean existsByToken(String token) {
        return jpaRepository.existsByToken(token);
    }

    @Override
    public Optional<TokenBlacklist> findByToken(String token) {
        return this.<Optional<TokenBlacklist>>fromJpa(jpaRepository.findByToken(token));
    }

    @Override
    public void deleteExpiredTokens(Date now) {
        jpaRepository.deleteExpiredTokens(now);
    }

    @Override
    public long countExpiredTokens(Date now) {
        return jpaRepository.countExpiredTokens(now);
    }

    @Override
    public TokenBlacklist save(TokenBlacklist entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<TokenBlacklist> saveAll(Iterable<TokenBlacklist> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<TokenBlacklist> findById(UUID id) {
        return this.<Optional<TokenBlacklist>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<TokenBlacklist> findAll() {
        return this.<List<TokenBlacklist>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<TokenBlacklist> findAllById(Iterable<UUID> ids) {
        return this.<List<TokenBlacklist>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(TokenBlacklist entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<TokenBlacklist> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public TokenBlacklist saveAndFlush(TokenBlacklist entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<TokenBlacklist> findAll(Pageable pageable) {
        return this.<Page<TokenBlacklist>>fromJpa(jpaRepository.findAll(pageable));
    }
}
