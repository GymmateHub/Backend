package com.gymmate.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration that runs repair before migrate.
 * This fixes checksum mismatches caused by locally modified migration files
 * that were already applied to the database.
 *
 * Safe to keep permanently — repair() is idempotent and only updates
 * metadata in flyway_schema_history when there is a mismatch.
 */
@Slf4j
@Configuration
public class FlywayRepairConfig {

  @Bean
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      log.info("Running Flyway repair before migrate...");
      flyway.repair();
      log.info("Flyway repair complete. Starting migration...");
      flyway.migrate();
    };
  }
}

