package com.gymmate.config;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

//@SpringBootTest
//@Testcontainers
//public abstract class BaseIntegrationTest {
//
//  @Container
//  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
//    .withDatabaseName("gymmate_test")
//    .withUsername("test")
//    .withPassword("test")
//    .withReuse(true);
//
//  @DynamicPropertySource
//  static void configureProperties(DynamicPropertyRegistry registry) {
//    registry.add("spring.datasource.url", postgres::getJdbcUrl);
//    registry.add("spring.datasource.username", postgres::getUsername);
//    registry.add("spring.datasource.password", postgres::getPassword);
//    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
//    registry.add("spring.jpa.show-sql", () -> "true");
//  }
//
//  @BeforeAll
//  static void setUp() {
//    postgres.start();
//  }
//}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    // H2 Database Configuration
    registry.add("spring.datasource.url",
      () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
    registry.add("spring.datasource.driver-class-name",
      () -> "org.h2.Driver");
    registry.add("spring.datasource.username", () -> "sa");
    registry.add("spring.datasource.password", () -> "");

    // Hibernate Configuration
    registry.add("spring.jpa.database-platform",
      () -> "org.hibernate.dialect.H2Dialect");
    registry.add("spring.h2.console.enabled", () -> "true");
    registry.add("spring.h2.console.path", () -> "/h2-console");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.jpa.show-sql", () -> "true");
    registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
  }
}
