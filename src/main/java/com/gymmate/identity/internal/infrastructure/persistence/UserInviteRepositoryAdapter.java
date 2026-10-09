package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.identity.internal.domain.UserInvite;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.UserInviteRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link UserInviteRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the UserInvite finders live here.
 */
@Component()
@Transactional()
public class UserInviteRepositoryAdapter extends JpaDomainRepositoryAdapter<UserInvite, UUID, UserInviteJpaRepository>
        implements UserInviteRepository {

    public UserInviteRepositoryAdapter(UserInviteJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<UserInvite> findByToken(String token) {
        return this.<Optional<UserInvite>>fromJpa(jpaRepository.findByToken(token));
    }

    @Override
    public Optional<UserInvite> findByTokenHash(String tokenHash) {
        return this.<Optional<UserInvite>>fromJpa(jpaRepository.findByTokenHash(tokenHash));
    }

    @Override
    public List<UserInvite> findByGymId(UUID gymId) {
        return this.<List<UserInvite>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<UserInvite> findByEmailAndGymId(String email, UUID gymId) {
        return this.<List<UserInvite>>fromJpa(jpaRepository.findByEmailAndGymId(email, gymId));
    }

    @Override
    public List<UserInvite> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime dateTime) {
        return this.<List<UserInvite>>fromJpa(jpaRepository.findByStatusAndExpiresAtBefore(status, dateTime));
    }
}
