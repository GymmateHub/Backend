package com.gymmate.user.domain;

import java.util.List;
import java.util.Optional;

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
    Optional<User> findById(Long id);
    
    /**
     * Find a user by their email address.
     */
    Optional<User> findByEmail(String email);
    
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
    void deleteById(Long id);
}