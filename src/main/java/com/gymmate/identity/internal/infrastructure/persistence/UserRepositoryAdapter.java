package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.UserRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link UserRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the User finders live here.
 */
@Component()
@Transactional()
public class UserRepositoryAdapter extends JpaDomainRepositoryAdapter<User, UUID, UserJpaRepository>
        implements UserRepository {

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
