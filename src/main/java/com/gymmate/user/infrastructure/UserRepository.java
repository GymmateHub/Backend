package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity.
 * Provides tenant-aware and system-wide query methods.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Email lookup methods
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndGymId(String email, UUID gymId);
    boolean existsByEmail(String email);

    // Role-based queries
    List<User> findByRole(UserRole role);
    List<User> findByRoleAndGymId(UserRole role, UUID gymId);

    // Status-based queries
    List<User> findByStatus(UserStatus status);
    List<User> findByStatusAndGymId(UserStatus status, UUID gymId);

    // Gym-based queries
    List<User> findByGymId(UUID gymId);
}
