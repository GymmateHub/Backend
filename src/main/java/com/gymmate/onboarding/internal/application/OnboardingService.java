package com.gymmate.onboarding.internal.application;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.dto.MemberRegistrationRequest;
import com.gymmate.identity.api.dto.NewUserRegistration;
import com.gymmate.identity.api.dto.OwnerRegistrationRequest;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.dto.InitialGymCommand;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.util.CurrencyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

/**
 * Self-service registration flows. Orchestrates identity (user accounts), organisation
 * (organisation hub + first gym) and — through organisation's
 * {@code OrganisationCreatedEvent} — billing (default trial subscription), each through
 * its public API, inside one transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final IdentityApi identityApi;
    private final OrganisationApi organisationApi;

    /**
     * Registers a gym owner: inactive user (pending OTP), organisation hub with default
     * subscription, and the organisation's first gym.
     *
     * @return the new owner's user id
     */
    @Transactional
    public UUID registerOwner(OwnerRegistrationRequest request) {
        log.info("Registering owner: {}", request.email());

        if (identityApi.emailExists(request.email())) {
            throw new DomainException("USER_ALREADY_EXISTS",
                    "A user with email '" + request.email() + "' already exists");
        }

        // 1. Create User (Inactive, waiting for OTP) — password policy enforced by identity
        UUID ownerId = identityApi.registerUser(new NewUserRegistration(
                request.email(), request.firstName(), request.lastName(), request.password(),
                request.phone(), UserRole.GYM_OWNER, null));

        // Fallback default values for optional fields
        String organisationName = StringUtils.hasText(request.organisationName())
                ? request.organisationName()
                : request.firstName() + "'s Organisation";
        String gymName = StringUtils.hasText(request.gymName())
                ? request.gymName()
                : request.firstName() + "'s Gym";
        String timezone = StringUtils.hasText(request.timezone())
                ? request.timezone()
                : "UTC";
        String country = StringUtils.hasText(request.country())
                ? request.country()
                : "United States";
        String phone = StringUtils.hasText(request.phone())
                ? request.phone()
                : "+10000000000";

        // 2. Create Organisation & Hub (organisation, default subscription, owner link)
        UUID organisationId = organisationApi.createHub(organisationName, request.email(), ownerId);

        // BUG-014: enforce the org's subscription tier gym limit even for the very first gym,
        // instead of only checking it on the /organisations/current/gyms endpoint.
        organisationApi.checkCanCreateGym(organisationId);

        // 3. Create initial Gym. Currency is resolved from the country the owner actually
        //    supplied, not the "United States" fallback above.
        organisationApi.createInitialGym(new InitialGymCommand(
                organisationId, ownerId, gymName, "Main Gym", request.email(), phone, timezone,
                CurrencyResolver.forCountry(request.country()), country));

        return ownerId;
    }

    /**
     * Public member self-registration through an organisation's join link.
     *
     * @return the new member's user id
     */
    @Transactional
    public UUID registerMember(MemberRegistrationRequest request) {
        log.info("Registering member: {}", request.email());

        if (request.gymSlug() == null) {
            throw new DomainException("INVALID_REQUEST", "Gym slug is required for public registration");
        }

        // "URL pattern: app.gymmatehub.com/join/:gymSlug" — the slug currently identifies the
        // organisation; members join its first active gym.
        UUID organisationId = organisationApi.getOrganisationIdBySlug(request.gymSlug());
        List<GymSummary> gyms = organisationApi.getActiveGyms(organisationId);
        if (gyms.isEmpty()) {
            throw new DomainException("NO_ACTIVE_GYM", "No active gym find for this link");
        }
        UUID gymId = gyms.get(0).id();

        if (identityApi.emailExists(request.email())) {
            throw new DomainException("USER_ALREADY_EXISTS", "User already exists. Please login.");
        }

        UUID userId = identityApi.registerUser(new NewUserRegistration(
                request.email(), request.firstName(), request.lastName(), request.password(),
                request.phone(), UserRole.MEMBER, organisationId));

        // BUG-004: public self-registration must also create the Member profile linked to the
        // gym, otherwise every member endpoint 404s for these users.
        identityApi.createMemberProfile(userId, gymId);

        return userId;
    }
}
