package com.gymmate.organisation;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.UserQueryPort;
import com.gymmate.organisation.api.spi.MembershipRevenueSource;
import com.gymmate.organisation.api.spi.PlatformInvoiceRevenueSource;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.event.OrganisationCreatedEvent;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Boots the organisation module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses
 * and of the SPIs it declares (implemented by other modules):
 * {@code identity.IdentityApi}, {@code identity.UserQueryPort}, {@code organisation.MembershipRevenueSource}, {@code organisation.PlatformInvoiceRevenueSource}.
 */
@ApplicationModuleTest
class OrganisationModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @MockitoBean
    UserQueryPort userQueryPort;

    @MockitoBean
    MembershipRevenueSource membershipRevenueSource;

    @MockitoBean
    PlatformInvoiceRevenueSource platformInvoiceRevenueSource;

    @Autowired
    OrganisationApi organisationApi;

    @Test
    void bootstrapsInIsolation() {
    }

    @Test
    void creatingAHubAnnouncesTheOrganisationAndLinksTheOwner(Scenario scenario) {
        UUID owner = UUID.randomUUID();

        scenario.stimulate(() -> organisationApi.createHub("Iron Temple " + owner, "owner@example.com", owner))
                .andWaitForEventOfType(OrganisationCreatedEvent.class)
                .matchingMappedValue(OrganisationCreatedEvent::ownerUserId, owner)
                .toArriveAndVerify((event, hubId) -> {
                    assertThat(event.organisationId()).isEqualTo(hubId);
                    assertThat(event.contactEmail()).isEqualTo("owner@example.com");
                    verify(identityApi).assignOrganisation(owner, hubId);
                });
    }
}
