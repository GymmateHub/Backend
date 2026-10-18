package com.gymmate.support;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base for Spring Modulith {@code @ApplicationModuleTest}s: each subclass boots a single module
 * (plus the shared kernel) against a real, Flyway-migrated PostgreSQL 18. Collaborating modules
 * are not started; the test replaces their public APIs with mocks, which proves the module
 * depends on nothing but those APIs.
 */
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
public abstract class ModuleIntegrationTest {

    protected static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("gymmate_it")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        // Flyway owns the schema; Hibernate only validates it against the entity mappings, so
        // any drift between a migration and an entity fails the integration tests.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.ai.openai.api-key", () -> "test-openai-key");
        registry.add("app.admin.email", () -> "admin@gymmate.test");
        registry.add("app.admin.password", () -> "Admin!Test123");
        registry.add("app.admin.firstName", () -> "System");
        registry.add("app.admin.lastName", () -> "Admin");
    }
}
