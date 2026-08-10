package com.gymmate.organisation.infrastructure;

import com.gymmate.organisation.domain.Organisation;
import com.gymmate.payment.application.port.OrganisationBillingInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements payment's {@link OrganisationBillingInfoProvider} port using this
 * module's own {@code OrganisationRepository} — see the port Javadoc for why
 * {@code StripePaymentService}/{@code PaymentNotificationService}/{@code SubscriptionService}
 * no longer call it directly.
 */
@Component
@RequiredArgsConstructor
public class PaymentOrganisationBillingInfoAdapter implements OrganisationBillingInfoProvider {

    private final OrganisationRepository organisationRepository;

    @Override
    public Optional<BillingInfo> findBillingInfo(UUID organisationId) {
        return organisationRepository.findById(organisationId).map(this::toBillingInfo);
    }

    private BillingInfo toBillingInfo(Organisation org) {
        return new BillingInfo(org.getId(), org.getName(), org.getContactEmail(), org.getBillingEmail(), org.getOwnerUserId());
    }
}
