package com.gymmate.identity.internal.application.port;

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

/**
 * Spring Data JPA repository for User entity.
 * Provides tenant-aware and system-wide query methods.
 */
public interface UserRepository {

    // Email lookup methods
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndOrganisationId(String email, UUID organisationId);
    boolean existsByEmail(String email);

    // Role-based queries
    List<User> findByRole(UserRole role);
    long countByRole(UserRole role);
    List<User> findByRoleAndOrganisationId(UserRole role, UUID organisationId);

    // Status-based queries
    List<User> findByStatus(UserStatus status);
    List<User> findByStatusAndOrganisationId(UserStatus status, UUID organisationId);

    // Organisation-based queries
    List<User> findByOrganisationId(UUID organisationId);

    // Count queries for analytics
    long countByOrganisationIdAndRole(UUID organisationId, UserRole role);
    long countByOrganisationIdAndRoleAndStatus(UUID organisationId, UserRole role, UserStatus status);

    // Count users with multiple roles (for staff count)
    long countByOrganisationIdAndRoleIn(UUID organisationId,
                                         Collection<UserRole> roles);

    // Count active users with multiple roles (for staff count)
    long countByOrganisationIdAndRoleInAndStatus(UUID organisationId,
                                                   Collection<UserRole> roles,
                                                   UserStatus status);

User save(User entity);

List<User> saveAll(Iterable<User> entities);

Optional<User> findById(UUID id);

boolean existsById(UUID id);

List<User> findAll();

List<User> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(User entity);

void deleteAll(Iterable<User> entities);

User saveAndFlush(User entity);

void flush();

Page<User> findAll(Pageable pageable);
}
