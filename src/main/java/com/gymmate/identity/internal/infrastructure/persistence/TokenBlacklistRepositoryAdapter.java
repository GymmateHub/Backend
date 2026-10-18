package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.TokenBlacklist;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.TokenBlacklistRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link TokenBlacklistRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the TokenBlacklist finders live here.
 */
@Component()
@Transactional()
public class TokenBlacklistRepositoryAdapter extends JpaDomainRepositoryAdapter<TokenBlacklist, UUID, TokenBlacklistJpaRepository>
        implements TokenBlacklistRepository {

    public TokenBlacklistRepositoryAdapter(TokenBlacklistJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
