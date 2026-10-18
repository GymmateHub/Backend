package com.gymmate.billing;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.notification.api.EmailApi;
import com.gymmate.notification.api.NotificationApi;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.event.OrganisationCreatedEvent;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the billing module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses:
 * {@code identity.IdentityApi}, {@code notification.EmailApi}, {@code notification.NotificationApi}, {@code organisation.OrganisationApi}.
 */
@ApplicationModuleTest
class BillingModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    IdentityApi identityApi;

    @MockitoBean
    EmailApi emailApi;

    @MockitoBean
    NotificationApi notificationApi;

    @MockitoBean
    OrganisationApi organisationApi;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void bootstrapsInIsolation() {
    }

    @Test
    void newOrganisationsGetAStarterTrialSubscription(Scenario scenario) {
        UUID orgId = UUID.randomUUID();
        jdbc.update("insert into organisations (id, name, slug) values (?, ?, ?)", orgId, "Org " + orgId, "org-" + orgId);

        scenario.publish(new OrganisationCreatedEvent(orgId, "Org", "owner@example.com", UUID.randomUUID()))
                .andWaitForStateChange(() -> jdbc.queryForList(
                        "select s.status, t.name as tier from subscriptions s join subscription_tiers t on t.id = s.tier_id"
                                + " where s.organisation_id = ?", orgId), rows -> !rows.isEmpty())
                .andVerify(rows -> {
                    assertThat(rows).hasSize(1);
                    assertThat(rows.get(0).get("tier")).isEqualTo("starter");
                });
    }
}
