package com.gymmate.identity.internal.application;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.dto.AccessTokenClaims;
import com.gymmate.identity.api.dto.MemberProfile;
import com.gymmate.identity.api.dto.NewUserRegistration;
import com.gymmate.identity.api.dto.RegistrationResponse;
import com.gymmate.identity.api.dto.UserResponse;
import com.gymmate.identity.api.dto.TokenPair;
import com.gymmate.identity.api.dto.UserSummary;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.identity.internal.application.port.MemberRepository;
import com.gymmate.identity.internal.application.port.UserRepository;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** Implementation of the identity module's public facade. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdentityApiService implements IdentityApi {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;

    // ---- users ----

    @Override
    public Optional<UserSummary> findUser(UUID userId) {
        return userRepository.findById(userId).map(IdentityApiService::toSummary);
    }

    @Override
    public UserSummary getUser(UUID userId) {
        return findUser(userId).orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    @Override
    @Transactional
    public void assignOrganisation(UUID userId, UUID organisationId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        user.setOrganisationId(organisationId);
        userRepository.save(user);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    @Override
    public long countUsersByOrganisationAndRole(UUID organisationId, UserRole role) {
        return userRepository.countByOrganisationIdAndRole(organisationId, role);
    }

    @Override
    public long countUsersByOrganisationAndRoles(UUID organisationId, Collection<UserRole> roles) {
        return userRepository.countByOrganisationIdAndRoleIn(organisationId, roles);
    }

    @Override
    public long countUsersByOrganisationAndRolesAndStatus(UUID organisationId, Collection<UserRole> roles,
                                                          UserStatus status) {
        return userRepository.countByOrganisationIdAndRoleInAndStatus(organisationId, roles, status);
    }

    // ---- members ----

    @Override
    public Optional<MemberProfile> findMember(UUID memberId) {
        return memberRepository.findById(memberId).map(IdentityApiService::toProfile);
    }

    @Override
    public MemberProfile getMember(UUID memberId) {
        return toProfile(memberService.findById(memberId));
    }

    @Override
    public MemberProfile getMemberByUserId(UUID userId) {
        return toProfile(memberService.findByUserId(userId));
    }

    @Override
    @Transactional
    public MemberProfile updateMemberFitnessGoals(UUID memberId, String[] fitnessGoals, String experienceLevel) {
        return toProfile(memberService.updateFitnessGoals(memberId, fitnessGoals, experienceLevel));
    }

    @Override
    public long countMembersByOrganisation(UUID organisationId) {
        return memberRepository.countByOrganisationId(organisationId);
    }

    @Override
    public long countMembersByOrganisationAndStatus(UUID organisationId, MemberStatus status) {
        return memberRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public long countMembersByGym(UUID gymId) {
        return memberRepository.countByGymId(gymId);
    }

    @Override
    public long countMembersByGymCreatedBetween(UUID gymId, LocalDateTime from, LocalDateTime to) {
        return memberRepository.countByGymIdAndCreatedAtBetween(gymId, from, to);
    }

    // ---- self-service registration ----

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public UUID registerUser(NewUserRegistration registration) {
        authenticationService.validatePassword(registration.rawPassword());

        User user = User.builder()
                .email(registration.email())
                .firstName(registration.firstName())
                .lastName(registration.lastName())
                .passwordHash(passwordService.encode(registration.rawPassword()))
                .phone(registration.phone())
                .role(registration.role())
                .status(UserStatus.INACTIVE) // requires OTP verification
                .emailVerified(false)
                .build();
        if (registration.organisationId() != null) {
            user.setOrganisationId(registration.organisationId());
        }
        return userRepository.save(user).getId();
    }

    @Override
    @Transactional
    public void createMemberProfile(UUID userId, UUID gymId) {
        memberService.createMember(userId, gymId, null);
    }

    @Override
    @Transactional
    public RegistrationResponse sendRegistrationOtp(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return authenticationService.sendOtpForUser(user);
    }

    @Override
    public UserResponse describeUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return UserResponse.fromEntity(user);
    }

    // ---- tokens ----

    @Override
    public TokenPair issueTokens(UUID userId, UUID gymId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        String access = gymId != null ? jwtService.generateToken(user, gymId) : jwtService.generateToken(user);
        return new TokenPair(access, jwtService.generateRefreshToken(user));
    }

    @Override
    public AccessTokenClaims parseAccessToken(String token) {
        return new AccessTokenClaims(jwtService.extractUserId(token), jwtService.extractOrganisationId(token),
                jwtService.extractGymId(token));
    }

    // ---- mapping ----

    static UserSummary toSummary(User user) {
        return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getRole(), user.getStatus(), user.getOrganisationId(), user.isActive(), user.isEmailVerified());
    }

    static MemberProfile toProfile(Member member) {
        return new MemberProfile(member.getId(), member.getUserId(), member.getOrganisationId(), member.getGymId(),
                member.getStatus(), member.isActive(), member.isWaiverSigned(), member.getFitnessGoals(),
                member.getExperienceLevel());
    }
}
