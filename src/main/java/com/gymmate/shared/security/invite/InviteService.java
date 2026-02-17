package com.gymmate.shared.security.invite;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.NotFoundException;
import com.gymmate.shared.security.AuthenticationService;
import com.gymmate.shared.security.JwtService;
import com.gymmate.shared.security.invite.dto.*;
import com.gymmate.shared.service.EmailService;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing user invitations.
 *
 * Handles the complete invite lifecycle:
 * - Creating invites (by OWNER/ADMIN)
 * - Validating invite tokens (when user clicks link)
 * - Accepting invites (user sets password and activates account)
 * - Resending expired invites
 * - Revoking invites
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {

    private final UserInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final GymRepository gymRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final AuthenticationService authenticationService;

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String frontendUrl;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_LENGTH = 32; // 256 bits

    // ========== Create Invite ==========

    /**
     * Create and send a new invite.
     *
     * @param gymId The gym to invite the user to
     * @param invitedById The user creating the invite (must be OWNER or ADMIN)
     * @param request The invite details
     * @return The created invite response
     */
    @Transactional
    public InviteResponse createInvite(UUID gymId, UUID invitedById, InviteCreateRequest request) {
        log.info("Creating invite for {} to gym {} with role {}", request.email(), gymId, request.role());

        // Validate role is invite-only
        validateInviteRole(request.role());

        // Get gym and verify it exists
        Gym gym = gymRepository.findById(gymId)
            .orElseThrow(() -> new NotFoundException("Gym not found"));

        // Check if user already exists in this organisation
        if (userRepository.findByEmailAndOrganisationId(request.email().toLowerCase(), gym.getOrganisationId()).isPresent()) {
            throw new DomainException("USER_EXISTS",
                "A user with this email already exists in this organisation");
        }

        // Check if pending invite already exists for this email at this gym
        if (inviteRepository.existsByEmailAndGymIdAndStatus(request.email().toLowerCase(), gymId, InviteStatus.PENDING)) {
            throw new DomainException("INVITE_EXISTS",
                "A pending invite already exists for this email at this gym");
        }

        // Generate secure token
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        // Create invite entity
        UserInvite invite = UserInvite.create(
            gymId,
            gym.getOrganisationId(),
            invitedById,
            request.email(),
            request.role(),
            request.firstName(),
            request.lastName(),
            tokenHash
        );

        UserInvite savedInvite = inviteRepository.save(invite);
        log.info("Invite created with ID: {}", savedInvite.getId());

        // Get inviter name for email
        String inviterName = userRepository.findById(invitedById)
            .map(User::getFullName)
            .orElse("A team member");

        // Send invite email
        sendInviteEmail(savedInvite, gym, rawToken, inviterName);

        return InviteResponse.fromEntity(savedInvite);
    }

    // ========== Validate Invite ==========

    /**
     * Validate an invite token and return context for the accept form.
     * Called when user clicks the invite link.
     */
    @Transactional(readOnly = true)
    public InviteValidateResponse validateInvite(String token) {
        String tokenHash = hashToken(token);

        UserInvite invite = inviteRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new DomainException("INVALID_TOKEN", "Invalid or expired invite token"));

        Gym gym = gymRepository.findById(invite.getGymId())
            .orElseThrow(() -> new NotFoundException("Gym not found"));

        String inviterName = userRepository.findById(invite.getInvitedBy())
            .map(User::getFullName)
            .orElse("A team member");

        return InviteValidateResponse.builder()
            .inviteToken(token)
            .email(invite.getEmail())
            .firstName(invite.getFirstName())
            .lastName(invite.getLastName())
            .role(invite.getRole())
            .gymName(gym.getName())
            .gymLogoUrl(gym.getLogoUrl())
            .invitedByName(inviterName)
            .organisationId(invite.getOrganisationId())
            .gymId(invite.getGymId())
            .expiresAt(invite.getExpiresAt())
            .expired(invite.isExpired())
            .build();
    }

    // ========== Accept Invite ==========

    /**
     * Accept an invite - create user account and return JWT tokens.
     * The user is immediately logged in after accepting.
     */
    @Transactional
    public InviteAcceptResponse acceptInvite(InviteAcceptRequest request) {
        String tokenHash = hashToken(request.inviteToken());

        UserInvite invite = inviteRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new DomainException("INVALID_TOKEN", "Invalid or expired invite token"));

        // Validate invite can be accepted
        if (!invite.canBeAccepted()) {
            if (invite.isExpired()) {
                throw new DomainException("INVITE_EXPIRED", "This invite has expired. Please request a new one.");
            }
            throw new DomainException("INVITE_INVALID",
                "This invite is no longer valid (status: " + invite.getStatus() + ")");
        }

        // Validate password
        authenticationService.validatePassword(request.password());

        // Determine final name values (use request if provided, else use invite pre-fill)
        String firstName = request.firstName() != null && !request.firstName().isBlank()
            ? request.firstName()
            : invite.getFirstName();
        String lastName = request.lastName() != null && !request.lastName().isBlank()
            ? request.lastName()
            : invite.getLastName();

        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
            throw new DomainException("NAME_REQUIRED", "First name and last name are required");
        }

        // Create user account
        User user = User.builder()
            .email(invite.getEmail())
            .firstName(firstName)
            .lastName(lastName)
            .passwordHash(passwordService.encode(request.password()))
            .phone(request.phone())
            .organisationId(invite.getOrganisationId())
            .role(invite.getRole())
            .status(UserStatus.ACTIVE)
            .emailVerified(true) // Email verified by virtue of clicking invite link
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created from invite: {} with role {}", savedUser.getEmail(), savedUser.getRole());

        // Mark invite as accepted
        invite.markAccepted();
        inviteRepository.save(invite);

        // Generate JWT tokens with gym context
        String accessToken = jwtService.generateToken(savedUser, invite.getGymId());
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());

        return InviteAcceptResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .role(savedUser.getRole())
            .organisationId(savedUser.getOrganisationId())
            .gymId(invite.getGymId())
            .emailVerified(true)
            .build();
    }

    // ========== Resend Invite ==========

    /**
     * Resend an expired or pending invite with a new token.
     */
    @Transactional
    public InviteResponse resendInvite(UUID inviteId, UUID requestedById) {
        UserInvite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("Invite not found"));

        if (invite.getStatus() == InviteStatus.ACCEPTED) {
            throw new DomainException("INVITE_ACCEPTED", "Cannot resend an already accepted invite");
        }
        if (invite.getStatus() == InviteStatus.REVOKED) {
            throw new DomainException("INVITE_REVOKED", "Cannot resend a revoked invite");
        }

        // Generate new token
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        invite.resend(tokenHash);
        UserInvite savedInvite = inviteRepository.save(invite);

        Gym gym = gymRepository.findById(invite.getGymId())
            .orElseThrow(() -> new NotFoundException("Gym not found"));

        String inviterName = userRepository.findById(requestedById)
            .map(User::getFullName)
            .orElse("A team member");

        // Send new invite email
        sendInviteEmail(savedInvite, gym, rawToken, inviterName);

        log.info("Invite {} resent to {}", inviteId, invite.getEmail());

        return InviteResponse.fromEntity(savedInvite);
    }

    // ========== Revoke Invite ==========

    /**
     * Revoke a pending invite.
     */
    @Transactional
    public void revokeInvite(UUID inviteId) {
        UserInvite invite = inviteRepository.findById(inviteId)
            .orElseThrow(() -> new NotFoundException("Invite not found"));

        invite.revoke();
        inviteRepository.save(invite);

        log.info("Invite {} revoked", inviteId);
    }

    // ========== List Invites ==========

    /**
     * Get all invites for a gym.
     */
    @Transactional(readOnly = true)
    public List<InviteResponse> getInvitesForGym(UUID gymId) {
        return inviteRepository.findByGymIdOrderByCreatedAtDesc(gymId)
            .stream()
            .map(InviteResponse::fromEntity)
            .toList();
    }

    /**
     * Get pending invites for a gym.
     */
    @Transactional(readOnly = true)
    public List<InviteResponse> getPendingInvitesForGym(UUID gymId) {
        return inviteRepository.findByGymIdAndStatusOrderByCreatedAtDesc(gymId, InviteStatus.PENDING)
            .stream()
            .map(InviteResponse::fromEntity)
            .toList();
    }

    // ========== Helper Methods ==========

    private void validateInviteRole(UserRole role) {
        if (role == UserRole.OWNER || role == UserRole.MEMBER || role == UserRole.SUPER_ADMIN) {
            throw new DomainException("INVALID_ROLE",
                "Cannot invite users with role " + role + ". Only ADMIN, TRAINER, STAFF can be invited.");
        }
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void sendInviteEmail(UserInvite invite, Gym gym, String rawToken, String inviterName) {
        String inviteLink = String.format("%s/invite/accept?token=%s", frontendUrl, rawToken);

        emailService.sendInviteEmail(
            invite.getEmail(),
            invite.getFirstName() != null ? invite.getFirstName() : "there",
            gym.getName(),
            invite.getRole().name(),
            inviterName,
            inviteLink,
            72 // hours until expiry
        );

        log.info("Invite email sent to {} for gym {}", invite.getEmail(), gym.getName());
    }
}

