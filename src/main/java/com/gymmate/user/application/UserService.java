package com.gymmate.user.application;

import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRepository;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for user management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Find a user by ID.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
    }
    
    /**
     * Find a user by email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Update user profile.
     */
    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, String phoneNumber) {
        User user = findById(userId);
        user.updateProfile(firstName, lastName, phoneNumber);
        return userRepository.save(user);
    }
    
    /**
     * Record user login.
     */
    @Transactional
    public void recordLogin(Long userId) {
        User user = findById(userId);
        user.updateLastLogin();
        userRepository.save(user);
    }
    
    /**
     * Deactivate a user account.
     */
    @Transactional
    public User deactivateUser(Long userId) {
        User user = findById(userId);
        user.deactivate();
        return userRepository.save(user);
    }
    
    /**
     * Activate a user account.
     */
    @Transactional
    public User activateUser(Long userId) {
        User user = findById(userId);
        user.activate();
        return userRepository.save(user);
    }
    
    /**
     * Find all users by role.
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }
    
    /**
     * Find all users by status.
     */
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    /**
     * Find all active gym owners.
     */
    public List<User> findActiveGymOwners() {
        return userRepository.findByRole(UserRole.GYM_OWNER)
                .stream()
                .filter(User::isActive)
                .toList();
    }
    
    /**
     * Find all users.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
}