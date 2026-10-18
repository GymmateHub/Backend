package com.gymmate.billing.internal.infrastructure.integration;

import com.gymmate.billing.internal.application.port.GymPaymentAccountPort;
import com.gymmate.billing.internal.domain.GymPaymentAccount;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.dto.StripeConnectState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Adapts the organisation module's gyms to billing's {@link GymPaymentAccountPort}. */
@Component
@RequiredArgsConstructor
class OrganisationGymPaymentAccountAdapter implements GymPaymentAccountPort {

    private final OrganisationApi organisationApi;

    @Override
    public Optional<GymPaymentAccount> findById(UUID gymId) {
        return organisationApi.findGym(gymId).map(OrganisationGymPaymentAccountAdapter::toAccount);
    }

    @Override
    public GymPaymentAccount save(GymPaymentAccount account) {
        organisationApi.updateStripeConnect(account.getId(), new StripeConnectState(
                account.getStripeConnectAccountId(),
                account.getStripeChargesEnabled(),
                account.getStripePayoutsEnabled(),
                account.getStripeDetailsSubmitted(),
                account.getStripeOnboardingCompletedAt()));
        return account;
    }

    private static GymPaymentAccount toAccount(GymSummary gym) {
        return GymPaymentAccount.builder()
                .id(gym.id())
                .organisationId(gym.organisationId())
                .name(gym.name())
                .contactEmail(gym.contactEmail())
                .stripeConnectAccountId(gym.stripeConnectAccountId())
                .stripeChargesEnabled(gym.stripeChargesEnabled())
                .stripePayoutsEnabled(gym.stripePayoutsEnabled())
                .stripeDetailsSubmitted(gym.stripeDetailsSubmitted())
                .stripeOnboardingCompletedAt(gym.stripeOnboardingCompletedAt())
                .build();
    }
}
