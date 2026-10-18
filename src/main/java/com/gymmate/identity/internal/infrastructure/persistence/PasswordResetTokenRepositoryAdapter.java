package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.PasswordResetTokenRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PasswordResetTokenRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the PasswordResetToken finders live here.
 */
@Component()
@Transactional()
public class PasswordResetTokenRepositoryAdapter extends JpaDomainRepositoryAdapter<PasswordResetToken, UUID, PasswordResetTokenJpaRepository>
        implements PasswordResetTokenRepository {

    public PasswordResetTokenRepositoryAdapter(PasswordResetTokenJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return this.<Optional<PasswordResetToken>>fromJpa(jpaRepository.findByToken(token));
    }

    @Override
    public void deleteByUser_Id(UUID userId) {
        jpaRepository.deleteByUser_Id(userId);
    }
}
