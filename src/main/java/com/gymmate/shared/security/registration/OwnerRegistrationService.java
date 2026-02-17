package com.gymmate.shared.security.registration;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.organisation.application.OrganisationService;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.security.AuthenticationService;
import com.gymmate.shared.security.TotpService;
import com.gymmate.shared.security.dto.OwnerRegistrationRequest;
import com.gymmate.shared.security.dto.OwnerRegistrationResponse;
import com.gymmate.shared.service.EmailService;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for owner (gym admin) self-registration.
 *
 * Registration flow:
 * 1. Create organisation with PENDING status
 * 2. Create user with INACTIVE status and emailVerified=false
 * 3. Assign user as organisation owner
 * 4. Create default gym with PENDING status
 * 5. Send OTP email for verification
 *
 * After OTP verification:
 * - User status becomes ACTIVE
 * - Organisation and gym status become ACTIVE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerRegistrationService {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final OrganisationService organisationService;
    private final PasswordService passwordService;
    private final AuthenticationService authenticationService;
    private final TotpService totpService;
    private final EmailService emailService;

    private static final int OTP_VALIDITY_MINUTES = 5;

    /**
     * Register a new gym owner with organisation and default gym.
     */
    @Transactional
    public OwnerRegistrationResponse registerOwner(OwnerRegistrationRequest request) {
        log.info("Registering new owner: email={}, org={}, gym={}",
            request.email(), request.organisationName(), request.gymName());

        // Check if user already exists (system-wide unique email for owners)
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with this email already exists");
        }

        // Validate password
        authenticationService.validatePassword(request.password());

        // Step 1: Create organisation
        String orgSlug = organisationService.generateSlug(request.organisationName());
        Organisation organisation = organisationService.createOrganisation(
            request.organisationName(),
            orgSlug,
            request.email()
        );
        log.info("Organisation created: {} (ID: {})", organisation.getName(), organisation.getId());

        // Step 2: Create user
        User user = User.builder()
            .email(request.email().toLowerCase().trim())
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .passwordHash(passwordService.encode(request.password()))
            .phone(request.phone())
            .organisationId(organisation.getId())
            .role(UserRole.OWNER)
            .status(UserStatus.INACTIVE)
            .emailVerified(false)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        // Step 3: Assign owner to organisation
        organisationService.assignOwner(organisation.getId(), savedUser.getId());

        // Step 4: Create default gym
        Gym gym = Gym.createDefault(
            request.gymName(),
            request.email(),
            request.phone() != null ? request.phone() : "",
            organisation.getId()
        );

        // Set optional fields if provided
        if (request.timezone() != null && !request.timezone().isBlank()) {
            gym.setTimezone(request.timezone());
        }
        if (request.country() != null && !request.country().isBlank()) {
            gym.setCountry(request.country());
        }

        Gym savedGym = gymRepository.save(gym);
        log.info("Gym created: {} (ID: {})", savedGym.getName(), savedGym.getId());

        // Step 5: Send OTP email
        String otp = totpService.generateOtp(savedUser.getId().toString());
        totpService.updateRateLimit(savedUser.getId().toString());

        emailService.sendOtpEmail(
            savedUser.getEmail(),
            savedUser.getFirstName(),
            otp,
            OTP_VALIDITY_MINUTES
        );
        log.info("OTP sent to: {}", savedUser.getEmail());

        return OwnerRegistrationResponse.builder()
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .organisationId(organisation.getId())
            .organisationName(organisation.getName())
            .gymId(savedGym.getId())
            .gymName(savedGym.getName())
            .message("Registration successful. Please check your email for the verification code.")
            .expiresIn(OTP_VALIDITY_MINUTES * 60)
            .build();
    }
}

