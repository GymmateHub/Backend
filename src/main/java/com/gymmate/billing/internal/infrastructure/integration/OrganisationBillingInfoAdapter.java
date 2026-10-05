package com.gymmate.billing.internal.infrastructure.integration;

import com.gymmate.billing.internal.application.port.OrganisationBillingInfoProvider;
import com.gymmate.organisation.api.OrganisationApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/** Adapts the organisation module's billing view to billing's {@link OrganisationBillingInfoProvider} port. */
@Component
@RequiredArgsConstructor
class OrganisationBillingInfoAdapter implements OrganisationBillingInfoProvider {

    private final OrganisationApi organisationApi;

    @Override
    public Optional<BillingInfo> findBillingInfo(UUID organisationId) {
        return organisationApi.findBillingInfo(organisationId)
                .map(o -> new BillingInfo(o.id(), o.name(), o.contactEmail(), o.billingEmail(), o.ownerUserId()));
    }
}
