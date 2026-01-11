-- ============================================================================
-- Flyway Repair Script for GymMate
-- ============================================================================
-- Run this against your PostgreSQL database to fix the Flyway schema history
-- after the V1.17 migration failure.
--
-- Connect to your Aiven PostgreSQL and run these commands:
-- ============================================================================

-- First, check the current state of the flyway history
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- Delete the failed V1.17 migration record (if it exists)
DELETE FROM flyway_schema_history
WHERE version = '1.17' AND success = false;

-- Verify the cleanup - should only show V1 as successful
SELECT installed_rank, version, description, success, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- ============================================================================
-- After running this, restart your Spring Boot application.
-- It should start successfully since:
-- 1. V1 (Complete_Schema) is already applied and recorded
-- 2. No more old migration files exist in target/
-- ============================================================================

