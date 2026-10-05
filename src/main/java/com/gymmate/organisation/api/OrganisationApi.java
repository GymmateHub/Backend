package com.gymmate.organisation.api;

import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.dto.InitialGymCommand;
import com.gymmate.organisation.api.dto.OrganisationBillingInfo;
import com.gymmate.organisation.api.dto.OrganisationInfo;
import com.gymmate.organisation.api.dto.StripeConnectState;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public facade of the organisation module (organisations and their gyms). Other modules
 * use this instead of the Organisation/Gym aggregates and repositories.
 */
public interface OrganisationApi {

    // ---- gyms ----

    Optional<GymSummary> findGym(UUID gymId);

    /** Active gyms of an organisation, in the same order as the gym listing endpoints. */
    List<GymSummary> getActiveGyms(UUID organisationId);

    /** Records the Stripe Connect payout-account state of a gym (owned by billing). */
    void updateStripeConnect(UUID gymId, StripeConnectState state);

    long countGyms();

    long countGymsByOrganisation(UUID organisationId);

    // ---- organisations ----

    Optional<OrganisationBillingInfo> findBillingInfo(UUID organisationId);

    /** @throws com.gymmate.shared.exception.ResourceNotFoundException when no organisation has that slug */
    UUID getOrganisationIdBySlug(String slug);

    long countOrganisations();

    List<OrganisationInfo> listOrganisations();

    /** The most recently created organisations, newest first. */
    List<OrganisationInfo> listRecentOrganisations(int limit);

    // ---- provisioning (onboarding) ----

    /**
     * Creates an organisation hub owned by the given user: organisation, owner link and
     * (via {@link com.gymmate.organisation.api.event.OrganisationCreatedEvent}) the default
     * subscription. Joins the caller's transaction.
     *
     * @return the new organisation's id
     */
    UUID createHub(String name, String contactEmail, UUID ownerUserId);

    /** @throws com.gymmate.shared.exception.DomainException when the plan's gym limit is reached */
    void checkCanCreateGym(UUID organisationId);

    /** Creates the first gym of a new organisation, bypassing the active-owner check. */
    UUID createInitialGym(InitialGymCommand command);
}
