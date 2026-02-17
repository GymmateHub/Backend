package com.gymmate.user.application;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.notification.application.EmailService;
import com.gymmate.shared.exception.BadRequestException;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.api.dto.InviteRequest;
import com.gymmate.user.api.dto.InviteResponse;
import com.gymmate.user.api.dto.ValidateInviteResponse;
import com.gymmate.user.domain.InviteStatus;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserInvite;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.infrastructure.UserInviteRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class InviteService {

    private final UserInviteRepository userInviteRepository;
    private final UserRepository userRepository;
    private final GymService gymService;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public InviteResponse createInvite(UUID gymId, InviteRequest request, UUID invitedByUserId) {
        Gym gym = gymService.getGymById(gymId);
        User inviter = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", invitedByUserId.toString()));

        // Check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("User with this email already exists");
        }

        // Check if pending invite exists
        List<UserInvite> existing = userInviteRepository.findByEmailAndGymId(request.email(), gymId);
        for (UserInvite invite : existing) {
            if (invite.getStatus() == InviteStatus.PENDING && !invite.isExpired()) {
                throw new BadRequestException("A pending invite already exists for this email");
            }
        }

        // Validate Role (Owner cannot be invited)
        if (request.role() == UserRole.OWNER || request.role() == UserRole.SUPER_ADMIN) {
            throw new BadRequestException("Owners and Super Admins cannot be invited via this flow");
        }

        String token = UUID.randomUUID().toString();
        String tokenHash = hashToken(token);

        UserInvite invite = UserInvite.builder()
                .gymId(gymId)
                .organisationId(gym.getOrganisationId())
                .invitedBy(invitedByUserId)
                .email(request.email())
                .role(request.role())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .token(token)
                .tokenHash(tokenHash)
                .status(InviteStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusHours(72))
                .build();

        userInviteRepository.save(invite);

        sendInviteEmail(invite, gym.getName(), inviter.getFullName());

        return InviteResponse.fromEntity(invite);
    }

    @Transactional(readOnly = true)
    public ValidateInviteResponse validateInvite(String token) {
        UserInvite invite = userInviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invite", "token", token));

        Gym gym = gymService.getGymById(invite.getGymId());
        User inviter = userRepository.findById(invite.getInvitedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User", invite.getInvitedBy().toString()));

        boolean expired = invite.isExpired() || invite.getStatus() != InviteStatus.PENDING;

        return new ValidateInviteResponse(
                token,
                invite.getEmail(),
                invite.getFirstName(),
                invite.getLastName(),
                invite.getRole(),
                gym.getName(),
                inviter.getFullName(),
                invite.getOrganisationId(),
                invite.getGymId(),
                invite.getExpiresAt(),
                expired);
    }

    @Transactional
    public void acceptInvite(String token) {
        UserInvite invite = userInviteRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invite", "token", token));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invite is not pending");
        }
        if (invite.isExpired()) {
            throw new BadRequestException("Invite has expired");
        }

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setAcceptedAt(LocalDateTime.now());
        userInviteRepository.save(invite);
    }

    @Transactional
    public InviteResponse resendInvite(UUID inviteId, UUID userId) {
        UserInvite invite = userInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite", inviteId.toString()));

        Gym gym = gymService.getGymById(invite.getGymId());
        User inviter = userRepository.findById(userId).orElseThrow();

        // Regenerate token/expiry
        String token = UUID.randomUUID().toString();
        invite.setToken(token);
        invite.setTokenHash(hashToken(token));
        invite.setExpiresAt(LocalDateTime.now().plusHours(72));
        invite.setStatus(InviteStatus.PENDING);

        userInviteRepository.save(invite);

        sendInviteEmail(invite, gym.getName(), inviter.getFullName());

        return InviteResponse.fromEntity(invite);
    }

    @Transactional
    public void revokeInvite(UUID inviteId) {
        UserInvite invite = userInviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Invite", inviteId.toString()));

        invite.setStatus(InviteStatus.REVOKED);
        userInviteRepository.save(invite);
    }

    public List<InviteResponse> getInvitesForGym(UUID gymId) {
        return userInviteRepository.findByGymId(gymId).stream()
                .map(InviteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void sendInviteEmail(UserInvite invite, String gymName, String inviterName) {
        String inviteLink = frontendUrl + "/invite?token=" + invite.getToken();
        emailService.sendHtmlEmail(
                invite.getEmail(),
                "You've been invited to join " + gymName,
                "Hello " + (invite.getFirstName() != null ? invite.getFirstName() : "") + ",<br><br>" +
                        inviterName + " has invited you to join " + gymName + " as a " + invite.getRole() + ".<br><br>"
                        +
                        "Click here to accept: <a href=\"" + inviteLink + "\">" + inviteLink + "</a>");
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
