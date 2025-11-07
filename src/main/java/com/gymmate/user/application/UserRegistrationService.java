package com.gymmate.user.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
                           String plainPassword, String phone, UserRole role) {
        // Check if user already exists in current gym context
        UUID currentGymId = TenantContext.getCurrentTenantId();
        if (currentGymId != null && userRepository.findByEmailAndGymId(email, currentGymId).isPresent()) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists in this gym");
        }

        // For ADMIN role (gym owners/admins), check system-wide unique email
        if (role == UserRole.ADMIN && userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists");
        }

        // Validate password requirements
        validatePassword(plainPassword);

        // Encode password
        String passwordHash = passwordService.encode(plainPassword);

        // Create new user using builder
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Register a new gym member.
     */
    public User registerMember(String email, String firstName, String lastName,
                             String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Members can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.MEMBER);
    }

    /**
     * Register a new gym admin/owner.
     */
    public User registerGymAdmin(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        // Check if user already exists (system-wide)
        if (userRepository.existsByEmail(email)) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with email '" + email + "' already exists");
        }

        // Validate password requirements
        validatePassword(plainPassword);

        // Encode password
        String passwordHash = passwordService.encode(plainPassword);

        // Create new gym admin
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordHash)
                .phone(phone)
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        // Gym admins can be created with or without gym context
        // If within gym context, they'll be assigned to that gym
        // If not, gymId will be set when they create/join a gym

        // Save and return
        return userRepository.save(user);
    }

    /**
     * Register a new trainer.
     */
    public User registerTrainer(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Trainers can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.TRAINER);
    }

    /**
     * Register a new staff member.
     */
    public User registerStaff(String email, String firstName, String lastName,
                               String plainPassword, String phone) {
        if (TenantContext.getCurrentTenantId() == null) {
            throw new DomainException("INVALID_REGISTRATION",
                "Staff can only be registered within a gym context");
        }
        return registerUser(email, firstName, lastName, plainPassword, phone, UserRole.STAFF);
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
