package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.identity.internal.domain.UserInvite;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.UserInviteRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link UserInviteRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class UserInviteRepositoryAdapter extends DomainRepositoryAdapter implements UserInviteRepository {

    private final UserInviteJpaRepository jpaRepository;

    public UserInviteRepositoryAdapter(UserInviteJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public UserInvite save(UserInvite entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<UserInvite> saveAll(Iterable<UserInvite> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<UserInvite> findById(UUID id) {
        return this.<Optional<UserInvite>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<UserInvite> findAll() {
        return this.<List<UserInvite>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<UserInvite> findAllById(Iterable<UUID> ids) {
        return this.<List<UserInvite>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(UserInvite entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<UserInvite> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public UserInvite saveAndFlush(UserInvite entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<UserInvite> findAll(Pageable pageable) {
        return this.<Page<UserInvite>>fromJpa(jpaRepository.findAll(pageable));
    }
}
