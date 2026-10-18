package com.gymmate.notification;

import com.gymmate.whitelabel.api.WhitelabelApi;
import com.gymmate.notification.api.spi.AudienceMemberIdsResolver;
import com.gymmate.notification.api.spi.GymAccessVerifier;
import com.gymmate.notification.api.spi.MemberDirectory;
import com.gymmate.notification.api.spi.OrganisationSettingsSource;
import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the notification module on its own (plus the shared kernel) against PostgreSQL.
 * Collaborating modules are replaced by mocks of exactly the public APIs this module uses
 * and of the SPIs it declares (implemented by other modules):
 * {@code whitelabel.WhitelabelApi}, {@code notification.AudienceMemberIdsResolver}, {@code notification.GymAccessVerifier}, {@code notification.MemberDirectory}, {@code notification.OrganisationSettingsSource}.
 */
@ApplicationModuleTest
class NotificationModuleTest extends ModuleIntegrationTest {

    @MockitoBean
    WhitelabelApi whitelabelApi;

    @MockitoBean
    AudienceMemberIdsResolver audienceMemberIdsResolver;

    @MockitoBean
    GymAccessVerifier gymAccessVerifier;

    @MockitoBean
    MemberDirectory memberDirectory;

    @MockitoBean
    OrganisationSettingsSource organisationSettingsSource;

    @Test
    void bootstrapsInIsolation() {
    }
}
