package com.gymmate.access;

import com.gymmate.access.domain.AccessCredential;
import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.CredentialType;
import com.gymmate.access.infrastructure.AccessCredentialRepository;
import com.gymmate.access.infrastructure.AccessEventRepository;
import com.gymmate.access.infrastructure.AccessPointRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the access-control entities and derived queries map correctly to a
 * real PostgreSQL instance (covers the H2-vs-Postgres drift gap, C1/C2). Uses a
 * uuidv7() shim so the schema builds on postgres:16.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
class AccessPersistenceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("gymmate_test")
      .withUsername("test")
      .withPassword("test")
      .withInitScript("db/testcontainers/uuidv7.sql");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    // The `test` profile (application-test.yml) forces H2Dialect, but this test
    // runs against a real Postgres container. hibernate.dialect (properties.*)
    // takes precedence over database-platform, so override BOTH — otherwise
    // Hibernate maps SqlTypes.JSON to a binary type and binds jsonb columns as
    // bytea ("column is of type jsonb but expression is of type bytea").
    registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    // Mirror production (application.yml: ddl-auto=${JPA_DDL_AUTO:update}, Flyway on):
    // Flyway builds the versioned schema (V1..V12) and Hibernate reconciles any
    // entity drift. Using ddl-auto=create-drop instead makes Hibernate emit DDL
    // for BaseEntity's id (IDENTITY + DEFAULT uuidv7()), which Postgres rejects.
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    // Spring AI's OpenAiChatModel is an eager bean that throws if the key is
    // blank; a dummy value lets the context load (no real OpenAI call is made).
    registry.add("spring.ai.openai.api-key", () -> "test-openai-key");
    // SuperAdminInitializer (ApplicationReadyEvent) requires non-blank admin
    // email + password; firstName/lastName already default to System/Admin.
    registry.add("app.admin.email", () -> "admin@gymmate.test");
    registry.add("app.admin.password", () -> "Admin!Test123");
  }

  @Autowired AccessPointRepository accessPointRepository;
  @Autowired AccessCredentialRepository accessCredentialRepository;
  @Autowired AccessEventRepository accessEventRepository;
  @Autowired JdbcTemplate jdbc;

  // access_* rows have NOT NULL FKs to organisations/gyms (and members for
  // credentials). Seed the minimal parent rows so the inserts satisfy the
  // foreign keys defined in V10__Access_Control_System.sql.
  private void seedOrgAndGym(UUID orgId, UUID gymId) {
    jdbc.update("INSERT INTO organisations (id, name, slug) VALUES (?, ?, ?)",
        orgId, "Test Org", "org-" + orgId);
    jdbc.update("INSERT INTO gyms (id, organisation_id, name, slug) VALUES (?, ?, ?, ?)",
        gymId, orgId, "Test Gym", "gym-" + gymId);
  }

  private UUID seedMember(UUID orgId, UUID gymId) {
    UUID userId = UUID.randomUUID();
    jdbc.update("INSERT INTO users (id, email) VALUES (?, ?)",
        userId, "member-" + userId + "@test.local");
    UUID memberId = UUID.randomUUID();
    jdbc.update("INSERT INTO members (id, organisation_id, gym_id, user_id) VALUES (?, ?, ?, ?)",
        memberId, orgId, gymId, userId);
    return memberId;
  }

  private AccessPoint persistPoint(UUID gymId, UUID orgId) {
    AccessPoint p = AccessPoint.builder().name("Main Door").mode(AccessPointMode.SOFTWARE)
        .reentryLockoutSeconds(300).build();
    p.setGymId(gymId);
    p.setOrganisationId(orgId);
    return accessPointRepository.save(p);
  }

  @Test
  void persistsAccessPointWithGeneratedId() {
    UUID gymId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    seedOrgAndGym(orgId, gymId);
    AccessPoint saved = persistPoint(gymId, orgId);

    assertNotNull(saved.getId());
    assertEquals(1, accessPointRepository.findByGymId(gymId).size());
  }

  @Test
  void findsCredentialByTokenHash() {
    UUID gymId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    seedOrgAndGym(orgId, gymId);
    UUID memberId = seedMember(orgId, gymId);
    AccessCredential c = AccessCredential.builder()
        .memberId(memberId).type(CredentialType.QR).tokenHash("hash-" + UUID.randomUUID())
        .build();
    c.setGymId(gymId);
    c.setOrganisationId(orgId);
    AccessCredential saved = accessCredentialRepository.save(c);

    Optional<AccessCredential> found =
        accessCredentialRepository.findByTokenHashAndActiveTrue(saved.getTokenHash());
    assertTrue(found.isPresent());
    assertEquals(saved.getId(), found.get().getId());
  }

  @Test
  void derivedQueriesForEventsWork() {
    UUID gymId = UUID.randomUUID();
    UUID orgId = UUID.randomUUID();
    UUID memberId = UUID.randomUUID();
    seedOrgAndGym(orgId, gymId);
    AccessPoint point = persistPoint(gymId, orgId);

    AccessEvent granted = AccessEvent.builder()
        .accessPointId(point.getId()).memberId(memberId)
        .direction(AccessDirection.IN).decision(AccessDecision.GRANTED).build();
    granted.setGymId(gymId);
    granted.setOrganisationId(orgId);
    accessEventRepository.save(granted);

    AccessEvent tailgate = AccessEvent.builder()
        .accessPointId(point.getId()).direction(AccessDirection.IN)
        .decision(AccessDecision.GRANTED).tailgatingSuspected(true)
        .validScanCount(1).devicePassCount(2).build();
    tailgate.setGymId(gymId);
    tailgate.setOrganisationId(orgId);
    accessEventRepository.save(tailgate);

    assertEquals(2, accessEventRepository.findByGymIdOrderByOccurredAtDesc(gymId).size());
    assertEquals(1, accessEventRepository
        .findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(gymId).size());

    Optional<AccessEvent> lastGranted = accessEventRepository
        .findTopByMemberIdAndDecisionOrderByOccurredAtDesc(memberId, AccessDecision.GRANTED);
    assertTrue(lastGranted.isPresent());
    assertEquals(AccessDirection.IN, lastGranted.get().getDirection());
  }
}
