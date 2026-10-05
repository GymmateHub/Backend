package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity.
 * Provides tenant-aware and system-wide query methods.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    // Email lookup methods
    Optional<UserJpaEntity> findByEmail(String email);

    Optional<UserJpaEntity> findByEmailAndOrganisationId(String email, UUID organisationId);

    boolean existsByEmail(String email);

    // Role-based queries
    List<UserJpaEntity> findByRole(UserRole role);

    long countByRole(UserRole role);

    List<UserJpaEntity> findByRoleAndOrganisationId(UserRole role, UUID organisationId);

    // Status-based queries
    List<UserJpaEntity> findByStatus(UserStatus status);

    List<UserJpaEntity> findByStatusAndOrganisationId(UserStatus status, UUID organisationId);

    // Organisation-based queries
    List<UserJpaEntity> findByOrganisationId(UUID organisationId);

    // Count queries for analytics
    long countByOrganisationIdAndRole(UUID organisationId, UserRole role);

    long countByOrganisationIdAndRoleAndStatus(UUID organisationId, UserRole role, UserStatus status);

    // Count users with multiple roles (for staff count)
    @Query("SELECT COUNT(u) FROM User u WHERE u.organisationId = :organisationId AND u.role IN :roles")
    long countByOrganisationIdAndRoleIn(@Param("organisationId") UUID organisationId, @Param("roles") Collection<UserRole> roles);

    // Count active users with multiple roles (for staff count)
    @Query("SELECT COUNT(u) FROM User u WHERE u.organisationId = :organisationId AND u.role IN :roles AND u.status = :status")
    long countByOrganisationIdAndRoleInAndStatus(@Param("organisationId") UUID organisationId, @Param("roles") Collection<UserRole> roles, @Param("status") UserStatus status);
}
