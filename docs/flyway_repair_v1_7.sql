-- Flyway Migration Repair Script for V1_7
-- Run this directly in PostgreSQL to fix the migration issue

-- Step 1: Check current state
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Step 2: Check if payment_methods table exists and its structure
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'payment_methods';

-- Step 3: If payment_methods table exists but doesn't have gym_id,
-- you have two options:

-- OPTION A: Add the missing gym_id column (if it makes sense for your data)
-- ALTER TABLE payment_methods ADD COLUMN IF NOT EXISTS gym_id UUID;

-- OPTION B: Drop the old payment_methods table if it's not being used
-- (V1_8 creates gym_payment_methods which is what the entity expects)
-- DROP TABLE IF EXISTS payment_methods CASCADE;

-- Step 4: Delete the failed migration record so it can be re-attempted
DELETE FROM flyway_schema_history
WHERE version = '1.7' AND success = false;

-- Step 5: If you want to skip V1_7 entirely and mark it as done
-- (only if you've manually verified all required tables exist)
-- INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, execution_time, success)
-- VALUES (
--     (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
--     '1.7',
--     'Create Subscription And RateLimit Tables',
--     'SQL',
--     'V1_7__Create_Subscription_And_RateLimit_Tables.sql',
--     0,
--     'manual_repair',
--     0,
--     true
-- );

-- Step 6: Verify the fix
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

