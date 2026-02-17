package com.gymmate.shared.security.registration;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.security.AuthenticationService;
import com.gymmate.shared.security.TotpService;
import com.gymmate.shared.security.dto.MemberRegistrationRequest;
import com.gymmate.shared.security.dto.MemberRegistrationResponse;
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
 * Service for member self-registration.
 *
 * Registration flow:
 * 1. Resolve gym from slug
 * 2. Validate user doesn't already exist in the organisation
 * 3. Create user with INACTIVE status and emailVerified=false
 * 4. Send OTP email for verification
 *
 * After OTP verification:
 * - User status becomes ACTIVE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberRegistrationService {

    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final PasswordService passwordService;
    private final AuthenticationService authenticationService;
    private final TotpService totpService;
    private final EmailService emailService;

    private static final int OTP_VALIDITY_MINUTES = 5;

    /**
     * Register a new gym member using gym slug.
     */
    @Transactional
    public MemberRegistrationResponse registerMember(MemberRegistrationRequest request) {
        log.info("Registering new member: email={}, gymSlug={}", request.email(), request.gymSlug());

        // Step 1: Resolve gym from slug
        Gym gym = gymRepository.findBySlug(request.gymSlug().toLowerCase())
            .orElseThrow(() -> new NotFoundException("Gym not found with slug: " + request.gymSlug()));

        // Validate gym is active
        if (!gym.isActive()) {
            throw new DomainException("GYM_NOT_ACTIVE", "This gym is not accepting new members at this time");
        }

        // Step 2: Check if user already exists in this organisation
        String email = request.email().toLowerCase().trim();
        if (userRepository.findByEmailAndOrganisationId(email, gym.getOrganisationId()).isPresent()) {
            throw new DomainException("USER_ALREADY_EXISTS",
                "A user with this email already exists at this gym");
        }

        // Validate password
        authenticationService.validatePassword(request.password());

        // Step 3: Create user
        User user = User.builder()
            .email(email)
            .firstName(request.firstName().trim())
            .lastName(request.lastName().trim())
            .passwordHash(passwordService.encode(request.password()))
            .phone(request.phone())
            .organisationId(gym.getOrganisationId())
            .role(UserRole.MEMBER)
            .status(UserStatus.INACTIVE)
            .emailVerified(false)
            .build();

        User savedUser = userRepository.save(user);
        log.info("Member created: {} (ID: {}) for gym {}", savedUser.getEmail(), savedUser.getId(), gym.getName());

        // Step 4: Send OTP email
        String otp = totpService.generateOtp(savedUser.getId().toString());
        totpService.updateRateLimit(savedUser.getId().toString());

        emailService.sendOtpEmail(
            savedUser.getEmail(),
            savedUser.getFirstName(),
            otp,
            OTP_VALIDITY_MINUTES
        );
        log.info("OTP sent to: {}", savedUser.getEmail());

        return MemberRegistrationResponse.builder()
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .gymId(gym.getId())
            .gymName(gym.getName())
            .message("Registration successful. Please check your email for the verification code.")
            .expiresIn(OTP_VALIDITY_MINUTES * 60)
            .build();
    }
}

