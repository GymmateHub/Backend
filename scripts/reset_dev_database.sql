-- ============================================================================
-- GymMate Development Database Reset Script
-- ============================================================================
-- This script completely resets the database for development purposes.
-- It drops ALL tables and lets JPA hibernate.ddl-auto=create recreate them.
--
-- RUN THIS SCRIPT MANUALLY IN YOUR POSTGRESQL CLIENT:
-- psql -d gymmate -f reset_dev_database.sql
--
-- AFTER RUNNING THIS SCRIPT:
-- 1. Stop the application
-- 2. Change application-dev.yml: hibernate.ddl-auto: create
-- 3. Start the application (tables will be created matching entities)
-- 4. Change application-dev.yml: hibernate.ddl-auto: update
-- 5. Optionally run the exercise seed SQL manually
-- ============================================================================

-- Disable triggers for cascade drops
SET session_replication_role = 'replica';

-- ============================================================================
-- DROP ALL TABLES
-- ============================================================================

-- Health & Fitness module
DROP TABLE IF EXISTS workout_exercises CASCADE;
DROP TABLE IF EXISTS workout_logs CASCADE;
DROP TABLE IF EXISTS wearable_syncs CASCADE;
DROP TABLE IF EXISTS progress_photos CASCADE;
DROP TABLE IF EXISTS fitness_goals CASCADE;
DROP TABLE IF EXISTS health_metrics CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS exercise_categories CASCADE;

-- Classes & Scheduling
DROP TABLE IF EXISTS class_waitlists CASCADE;
DROP TABLE IF EXISTS class_bookings CASCADE;
DROP TABLE IF EXISTS class_schedules CASCADE;
DROP TABLE IF EXISTS classes CASCADE;
DROP TABLE IF EXISTS class_categories CASCADE;
DROP TABLE IF EXISTS personal_training_sessions CASCADE;
DROP TABLE IF EXISTS gym_areas CASCADE;

-- Membership
DROP TABLE IF EXISTS member_memberships CASCADE;
DROP TABLE IF EXISTS membership_plans CASCADE;
DROP TABLE IF EXISTS freeze_policies CASCADE;

-- Payments & Billing
DROP TABLE IF EXISTS refund_audit_log CASCADE;
DROP TABLE IF EXISTS payment_refunds CASCADE;
DROP TABLE IF EXISTS refund_requests CASCADE;
DROP TABLE IF EXISTS invoice_line_items CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS refunds CASCADE;
DROP TABLE IF EXISTS gym_invoices CASCADE;
DROP TABLE IF EXISTS member_invoices CASCADE;
DROP TABLE IF EXISTS member_payment_methods CASCADE;
DROP TABLE IF EXISTS gym_payment_methods CASCADE;
DROP TABLE IF EXISTS payment_methods CASCADE;
DROP TABLE IF EXISTS stripe_webhook_events CASCADE;

-- POS
DROP TABLE IF EXISTS pos_sale_items CASCADE;
DROP TABLE IF EXISTS pos_sales CASCADE;

-- Subscriptions
DROP TABLE IF EXISTS subscription_usage CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS gym_subscriptions CASCADE;
DROP TABLE IF EXISTS subscription_tiers CASCADE;
DROP TABLE IF EXISTS api_rate_limits CASCADE;

-- Inventory & Equipment
DROP TABLE IF EXISTS inventory_transactions CASCADE;
DROP TABLE IF EXISTS inventory_items CASCADE;
DROP TABLE IF EXISTS inventory_categories CASCADE;
DROP TABLE IF EXISTS equipment_maintenance CASCADE;
DROP TABLE IF EXISTS equipment CASCADE;
DROP TABLE IF EXISTS equipment_categories CASCADE;
DROP TABLE IF EXISTS maintenance_schedules CASCADE;
DROP TABLE IF EXISTS maintenance_records CASCADE;
DROP TABLE IF EXISTS stock_movements CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;

-- Access Control
DROP TABLE IF EXISTS access_logs CASCADE;
DROP TABLE IF EXISTS member_access_cards CASCADE;
DROP TABLE IF EXISTS access_points CASCADE;

-- Communication
DROP TABLE IF EXISTS message_queue CASCADE;
DROP TABLE IF EXISTS automated_messages CASCADE;
DROP TABLE IF EXISTS marketing_campaigns CASCADE;
DROP TABLE IF EXISTS communication_preferences CASCADE;

-- Analytics & Audit
DROP TABLE IF EXISTS analytics_events CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- Users & Staff
DROP TABLE IF EXISTS trainers CASCADE;
DROP TABLE IF EXISTS staff CASCADE;
DROP TABLE IF EXISTS members CASCADE;
DROP TABLE IF EXISTS password_reset_tokens CASCADE;
DROP TABLE IF EXISTS token_blacklist CASCADE;
DROP TABLE IF EXISTS pending_registrations CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Core
DROP TABLE IF EXISTS gym_settings CASCADE;
DROP TABLE IF EXISTS gyms CASCADE;
DROP TABLE IF EXISTS organisations CASCADE;

-- Workouts (original schema)
DROP TABLE IF EXISTS workouts CASCADE;
DROP TABLE IF EXISTS workout_templates CASCADE;

-- Flyway history (remove if starting fresh)
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Re-enable triggers
SET session_replication_role = 'origin';

-- ============================================================================
-- VERIFY CLEANUP
-- ============================================================================

DO $$
DECLARE
    table_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO table_count
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';

    RAISE NOTICE '============================================';
    RAISE NOTICE 'Database Reset Complete';
    RAISE NOTICE 'Remaining tables: %', table_count;
    RAISE NOTICE '============================================';

    IF table_count > 0 THEN
        RAISE NOTICE 'WARNING: Some tables remain. Check manually.';
    ELSE
        RAISE NOTICE 'All tables dropped successfully!';
        RAISE NOTICE '';
        RAISE NOTICE 'Next Steps:';
        RAISE NOTICE '1. Edit application-dev.yml: jpa.hibernate.ddl-auto: create';
        RAISE NOTICE '2. Start the application';
        RAISE NOTICE '3. Change back to: jpa.hibernate.ddl-auto: update';
        RAISE NOTICE '4. Run seed data scripts if needed';
    END IF;
END $$;

