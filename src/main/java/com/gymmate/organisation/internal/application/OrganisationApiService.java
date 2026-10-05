package com.gymmate.organisation.internal.application;

import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.dto.InitialGymCommand;
import com.gymmate.organisation.api.dto.OrganisationBillingInfo;
import com.gymmate.organisation.api.dto.OrganisationInfo;
import com.gymmate.organisation.api.dto.StripeConnectState;
import com.gymmate.organisation.internal.application.port.GymRepository;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.organisation.internal.domain.Organisation;
import com.gymmate.organisation.internal.infrastructure.persistence.OrganisationRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Implementation of the organisation module's public facade. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationApiService implements OrganisationApi {

    private final GymRepository gymRepository;
    private final OrganisationRepository organisationRepository;
    private final GymService gymService;
    private final OrganisationService organisationService;
    private final OrganisationLimitService organisationLimitService;

    // ---- gyms ----

    @Override
    public Optional<GymSummary> findGym(UUID gymId) {
        return gymRepository.findById(gymId).map(OrganisationApiService::toSummary);
    }

    @Override
    public List<GymSummary> getActiveGyms(UUID organisationId) {
        return gymService.getActiveGymsByOrganisation(organisationId).stream()
                .map(OrganisationApiService::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public void updateStripeConnect(UUID gymId, StripeConnectState state) {
        Gym gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym", gymId.toString()));
        gym.setStripeConnectAccountId(state.accountId());
        gym.setStripeChargesEnabled(state.chargesEnabled());
        gym.setStripePayoutsEnabled(state.payoutsEnabled());
        gym.setStripeDetailsSubmitted(state.detailsSubmitted());
        gym.setStripeOnboardingCompletedAt(state.onboardingCompletedAt());
        gymRepository.save(gym);
    }

    @Override
    public long countGyms() {
        return gymRepository.count();
    }

    @Override
    public long countGymsByOrganisation(UUID organisationId) {
        return gymRepository.countByOrganisationId(organisationId);
    }

    // ---- organisations ----

    @Override
    public Optional<OrganisationBillingInfo> findBillingInfo(UUID organisationId) {
        return organisationRepository.findById(organisationId)
                .map(org -> new OrganisationBillingInfo(org.getId(), org.getName(), org.getContactEmail(),
                        org.getBillingEmail(), org.getOwnerUserId()));
    }

    @Override
    public UUID getOrganisationIdBySlug(String slug) {
        return organisationService.getBySlug(slug).getId();
    }

    @Override
    public long countOrganisations() {
        return organisationRepository.count();
    }

    @Override
    public List<OrganisationInfo> listOrganisations() {
        return organisationRepository.findAll().stream()
                .map(OrganisationApiService::toSummary)
                .toList();
    }

    @Override
    public List<OrganisationInfo> listRecentOrganisations(int limit) {
        return organisationRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent().stream()
                .map(OrganisationApiService::toSummary)
                .toList();
    }

    // ---- provisioning ----

    @Override
    @Transactional
    public UUID createHub(String name, String contactEmail, UUID ownerUserId) {
        return organisationService.createHub(name, contactEmail, ownerUserId).getId();
    }

    @Override
    public void checkCanCreateGym(UUID organisationId) {
        organisationLimitService.checkCanCreateGym(organisationId);
    }

    @Override
    @Transactional
    public UUID createInitialGym(InitialGymCommand command) {
        // Mirrors the original registration flow: constructed directly (bypassing
        // GymService.registerGym's active-owner check), then bound to the organisation.
        Gym gym = new Gym(command.name(), command.description(), command.email(), command.phone(),
                command.ownerUserId());
        gym.setOrganisationId(command.organisationId());
        gym.setTimezone(command.timezone());
        gym.setCurrency(command.currency());
        gym.updateAddress(null, null, null, command.country(), null);
        return gymService.saveGym(gym).getId();
    }

    // ---- mapping ----

    static GymSummary toSummary(Gym gym) {
        return new GymSummary(gym.getId(), gym.getOrganisationId(), gym.getName(), gym.getContactEmail(),
                gym.getCity(), gym.getCountry(), gym.getTimezone(), gym.getCurrency(), gym.getStatus(),
                gym.isActive(), gym.getStripeConnectAccountId(), gym.getStripeChargesEnabled(),
                gym.getStripePayoutsEnabled(), gym.getStripeDetailsSubmitted(), gym.getStripeOnboardingCompletedAt());
    }

    static OrganisationInfo toSummary(Organisation org) {
        return new OrganisationInfo(org.getId(), org.getName(), org.getSlug(), org.getContactEmail(),
                org.getOwnerUserId(), org.getSubscriptionPlan(), org.getSubscriptionStatus(), org.isActive(),
                org.getCreatedAt());
    }
}
