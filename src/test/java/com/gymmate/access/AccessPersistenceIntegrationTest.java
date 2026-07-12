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
    registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    // Mirror production: build the schema with the real Flyway migrations and let
    // Hibernate only validate. Using ddl-auto=create-drop instead makes Hibernate
    // emit DDL for BaseEntity's id (@GeneratedValue(IDENTITY) + DEFAULT uuidv7()),
    // which Postgres rejects with "both default and identity specified".
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.locations", () -> "classpath:db/migration");
  }

  @Autowired AccessPointRepository accessPointRepository;
  @Autowired AccessCredentialRepository accessCredentialRepository;
  @Autowired AccessEventRepository accessEventRepository;

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
    AccessPoint saved = persistPoint(gymId, UUID.randomUUID());

    assertNotNull(saved.getId());
    assertEquals(1, accessPointRepository.findByGymId(gymId).size());
  }

  @Test
  void findsCredentialByTokenHash() {
    UUID gymId = UUID.randomUUID();
    AccessCredential c = AccessCredential.builder()
        .memberId(UUID.randomUUID()).type(CredentialType.QR).tokenHash("hash-" + UUID.randomUUID())
        .build();
    c.setGymId(gymId);
    c.setOrganisationId(UUID.randomUUID());
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
