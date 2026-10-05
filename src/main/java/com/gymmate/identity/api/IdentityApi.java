package com.gymmate.identity.api;

import com.gymmate.identity.api.dto.AccessTokenClaims;
import com.gymmate.identity.api.dto.MemberProfile;
import com.gymmate.identity.api.dto.NewUserRegistration;
import com.gymmate.identity.api.dto.RegistrationResponse;
import com.gymmate.identity.api.dto.UserResponse;
import com.gymmate.identity.api.dto.TokenPair;
import com.gymmate.identity.api.dto.UserSummary;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Public facade of the identity module. Other modules read users/members and request
 * identity-owned changes exclusively through this interface; identity's aggregates,
 * repositories and services stay internal.
 */
public interface IdentityApi {

    // ---- users ----

    Optional<UserSummary> findUser(UUID userId);

    /** @throws com.gymmate.shared.exception.ResourceNotFoundException if the user does not exist */
    UserSummary getUser(UUID userId);

    /** Links a user to the organisation they own/belong to. */
    void assignOrganisation(UUID userId, UUID organisationId);

    long countUsers();

    long countUsersByRole(UserRole role);

    long countUsersByOrganisationAndRole(UUID organisationId, UserRole role);

    long countUsersByOrganisationAndRoles(UUID organisationId, Collection<UserRole> roles);

    long countUsersByOrganisationAndRolesAndStatus(UUID organisationId, Collection<UserRole> roles, UserStatus status);

    // ---- members ----

    Optional<MemberProfile> findMember(UUID memberId);

    /** @throws com.gymmate.shared.exception.ResourceNotFoundException if the member does not exist */
    MemberProfile getMember(UUID memberId);

    /** @throws com.gymmate.shared.exception.ResourceNotFoundException if the user has no member profile */
    MemberProfile getMemberByUserId(UUID userId);

    MemberProfile updateMemberFitnessGoals(UUID memberId, String[] fitnessGoals, String experienceLevel);

    long countMembersByOrganisation(UUID organisationId);

    long countMembersByOrganisationAndStatus(UUID organisationId, MemberStatus status);

    long countMembersByGym(UUID gymId);

    long countMembersByGymCreatedBetween(UUID gymId, LocalDateTime from, LocalDateTime to);

    // ---- self-service registration (onboarding) ----

    boolean emailExists(String email);

    /**
     * Creates an INACTIVE, unverified user after enforcing the password policy.
     * Joins the caller's transaction.
     *
     * @return the new user's id
     * @throws com.gymmate.shared.exception.DomainException WEAK_PASSWORD when the policy rejects the password
     */
    UUID registerUser(NewUserRegistration registration);

    /** Creates the member profile linking a user to their home gym. */
    void createMemberProfile(UUID userId, UUID gymId);

    /** Sends the e-mail verification OTP to a newly registered user. */
    RegistrationResponse sendRegistrationOtp(UUID userId);

    /** The user as rendered by the identity REST API. */
    UserResponse describeUser(UUID userId);

    // ---- tokens ----

    /** Issues a new token pair for the user, scoped to the given gym context. */
    TokenPair issueTokens(UUID userId, UUID gymId);

    /** Reads the tenant claims of an access token (the token must already have been authenticated). */
    AccessTokenClaims parseAccessToken(String token);
}
