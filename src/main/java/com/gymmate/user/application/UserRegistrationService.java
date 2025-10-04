package com.gymmate.user.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRepository;
import com.gymmate.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for user registration use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationService {
    
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    
    /**
     * Register a new user in the system.
     */
    public User registerUser(String email, String firstName, String lastName, 
                           String plainPassword, String phoneNumber, UserRole role) {
        
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS", 
                "A user with email '" + email + "' already exists");
        }
        
        // Validate password requirements
        validatePassword(plainPassword);
        
        // Encode password
        String passwordHash = passwordService.encode(plainPassword);
        
        // Create new user
        User user = new User(email, firstName, lastName, passwordHash, phoneNumber, role);
        
        // Save and return
        return userRepository.save(user);
    }
    
    /**
     * Register a new gym member.
     */
    public User registerMember(String email, String firstName, String lastName, 
                             String plainPassword, String phoneNumber) {
        return registerUser(email, firstName, lastName, plainPassword, phoneNumber, UserRole.MEMBER);
    }
    
    /**
     * Register a new gym owner.
     */
    public User registerGymOwner(String email, String firstName, String lastName, 
                               String plainPassword, String phoneNumber) {
        return registerUser(email, firstName, lastName, plainPassword, phoneNumber, UserRole.GYM_OWNER);
    }
    
    /**
     * Register a new trainer.
     */
    public User registerTrainer(String email, String firstName, String lastName, 
                              String plainPassword, String phoneNumber) {
        return registerUser(email, firstName, lastName, plainPassword, phoneNumber, UserRole.TRAINER);
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().length() < 8) {
            throw new DomainException("WEAK_PASSWORD", 
                "Password must be at least 8 characters long");
        }
        
        // Add more password validation rules as needed
        if (!password.matches(".*[A-Z].*")) {
            throw new DomainException("WEAK_PASSWORD", 
                "Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new DomainException("WEAK_PASSWORD", 
                "Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            throw new DomainException("WEAK_PASSWORD", 
                "Password must contain at least one digit");
        }
    }
}