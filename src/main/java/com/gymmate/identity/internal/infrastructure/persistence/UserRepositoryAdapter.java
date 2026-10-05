package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.UserRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link UserRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class UserRepositoryAdapter extends DomainRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return this.<Optional<User>>fromJpa(jpaRepository.findByEmail(email));
    }

    @Override
    public Optional<User> findByEmailAndOrganisationId(String email, UUID organisationId) {
        return this.<Optional<User>>fromJpa(jpaRepository.findByEmailAndOrganisationId(email, organisationId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return this.<List<User>>fromJpa(jpaRepository.findByRole(role));
    }

    @Override
    public long countByRole(UserRole role) {
        return jpaRepository.countByRole(role);
    }

    @Override
    public List<User> findByRoleAndOrganisationId(UserRole role, UUID organisationId) {
        return this.<List<User>>fromJpa(jpaRepository.findByRoleAndOrganisationId(role, organisationId));
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return this.<List<User>>fromJpa(jpaRepository.findByStatus(status));
    }

    @Override
    public List<User> findByStatusAndOrganisationId(UserStatus status, UUID organisationId) {
        return this.<List<User>>fromJpa(jpaRepository.findByStatusAndOrganisationId(status, organisationId));
    }

    @Override
    public List<User> findByOrganisationId(UUID organisationId) {
        return this.<List<User>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public long countByOrganisationIdAndRole(UUID organisationId, UserRole role) {
        return jpaRepository.countByOrganisationIdAndRole(organisationId, role);
    }

    @Override
    public long countByOrganisationIdAndRoleAndStatus(UUID organisationId, UserRole role, UserStatus status) {
        return jpaRepository.countByOrganisationIdAndRoleAndStatus(organisationId, role, status);
    }

    @Override
    public long countByOrganisationIdAndRoleIn(UUID organisationId, Collection<UserRole> roles) {
        return jpaRepository.countByOrganisationIdAndRoleIn(organisationId, roles);
    }

    @Override
    public long countByOrganisationIdAndRoleInAndStatus(UUID organisationId, Collection<UserRole> roles, UserStatus status) {
        return jpaRepository.countByOrganisationIdAndRoleInAndStatus(organisationId, roles, status);
    }

    @Override
    public User save(User entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<User> saveAll(Iterable<User> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return this.<Optional<User>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<User> findAll() {
        return this.<List<User>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<User> findAllById(Iterable<UUID> ids) {
        return this.<List<User>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(User entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<User> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public User saveAndFlush(User entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return this.<Page<User>>fromJpa(jpaRepository.findAll(pageable));
    }
}
