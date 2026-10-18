package com.gymmate.shared.security;

import com.gymmate.shared.constants.UserRole;
import org.junit.jupiter.api.Test;

import static com.gymmate.shared.security.Permission.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The role model agreed for GymMateHub (see RolePermissions). */
class RolePermissionsTest {

    @Test
    void gymOwnerAdministersTheOrganisationButNotThePlatform() {
        assertThat(RolePermissions.of(UserRole.GYM_OWNER))
                .contains(ORG_BILLING, STAFF_INVITE, STAFF_MANAGE, ORG_MANAGE, MEMBERS_MANAGE, ANALYTICS_VIEW, HEALTH_MANAGE)
                .doesNotContain(PLATFORM_ADMIN, SELF_SERVICE);
    }

    @Test
    void onlyTheOwnerInvitesAndBills() {
        for (UserRole role : new UserRole[]{UserRole.MANAGER, UserRole.STAFF, UserRole.TRAINER, UserRole.MEMBER}) {
            assertThat(RolePermissions.of(role)).as(role.name()).doesNotContain(STAFF_INVITE, STAFF_MANAGE, ORG_BILLING);
        }
    }

    @Test
    void invitesAreForManagersStaffAndTrainersOnly() {
        assertThat(RolePermissions.INVITABLE_ROLES).containsExactlyInAnyOrder(UserRole.MANAGER, UserRole.STAFF, UserRole.TRAINER);
    }

    @Test
    void managerRunsOperationsAndSettings() {
        assertThat(RolePermissions.of(UserRole.MANAGER))
                .contains(ORG_MANAGE, PLANS_MANAGE, ANALYTICS_VIEW, MEMBERS_MANAGE, LEADS_MANAGE, INVENTORY_MANAGE, CLASSES_MANAGE, HEALTH_MANAGE);
    }

    @Test
    void staffWorksTheFrontDeskWithoutHealthOrSettings() {
        assertThat(RolePermissions.of(UserRole.STAFF))
                .contains(MEMBERS_MANAGE, LEADS_MANAGE, POS_OPERATE, INVENTORY_MANAGE, CLASSES_VIEW, BOOKINGS_MANAGE)
                .doesNotContain(HEALTH_MANAGE, ORG_MANAGE, PLANS_MANAGE, ANALYTICS_VIEW, CLASSES_MANAGE);
    }

    @Test
    void trainerRunsClassesAndHealthButNotSales() {
        assertThat(RolePermissions.of(UserRole.TRAINER))
                .contains(CLASSES_MANAGE, BOOKINGS_MANAGE, HEALTH_MANAGE, MEMBERS_VIEW)
                .doesNotContain(MEMBERS_MANAGE, LEADS_MANAGE, POS_OPERATE, INVENTORY_MANAGE);
    }

    @Test
    void memberHasOnlySelfServiceAndCatalogues() {
        assertThat(RolePermissions.of(UserRole.MEMBER)).containsExactlyInAnyOrder(SELF_SERVICE, PLANS_VIEW, CLASSES_VIEW);
    }

    @Test
    void superAdminHasThePlatformAndCanActInsideOrganisations() {
        assertThat(RolePermissions.of(UserRole.SUPER_ADMIN)).contains(PLATFORM_ADMIN, ORG_BILLING, STAFF_INVITE).doesNotContain(SELF_SERVICE);
    }
}
