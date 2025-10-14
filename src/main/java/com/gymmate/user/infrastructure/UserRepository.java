package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User aggregate.
 */
public interface UserRepository {

    /**
     * Save a user to the repository.
     */
    User save(User user);

    /**
     * Find a user by their unique identifier.
     */
    Optional<User> findById(UUID id);

    /**
     * Find a user by their email address.
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndGymId(String email, String gymId);

    /**
     * Check if a user exists with the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role.
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all users with a specific status.
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Find all users.
     */
    List<User> findAll();

    /**
     * Delete a user by their identifier.
     */
    void deleteById(UUID id);

    boolean existsById(UUID id);
}
