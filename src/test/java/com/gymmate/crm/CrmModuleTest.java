package com.gymmate.crm;

import com.gymmate.support.ModuleIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;


/**
 * Boots the crm module on its own (plus the shared kernel) against PostgreSQL.
 * The module depends on no other module.
 */
@ApplicationModuleTest
class CrmModuleTest extends ModuleIntegrationTest {

    @Test
    void bootstrapsInIsolation() {
    }
}
