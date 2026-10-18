-- ============================================================================
-- GymMate Pre-Go-Live Database Cleanup Script
-- ============================================================================
-- PURPOSE:
-- Safely purges all test data (tenants, gyms, users, workouts, bookings,
-- payments, POS, inventory, access logs, marketing, and notifications)
-- while PRESERVING table schemas, Flyway migration history, and system reference
-- catalogs (subscription tiers and exercise categories).
--
-- RUNNING ON VPS:
-- Inside the VPS or Docker container:
--   psql -U gymmate -d gymmate -f cleanup_test_data_before_golive.sql
-- Or via docker exec:
--   docker exec -i <postgres-container> psql -U gymmate -d gymmate < cleanup_test_data_before_golive.sql
-- ============================================================================

\set ON_ERROR_STOP on

BEGIN;

-- 1. Disable FK triggers for fast, safe truncate
SET session_replication_role = 'replica';

-- ============================================================================
-- 2. TRUNCATE ALL TRANSACTIONAL & TENANT DATA
-- ============================================================================

-- Access Control & IoT
TRUNCATE TABLE access_logs CASCADE;
TRUNCATE TABLE access_events CASCADE;
TRUNCATE TABLE access_schedules CASCADE;
TRUNCATE TABLE door_benefits CASCADE;
TRUNCATE TABLE access_credentials CASCADE;
TRUNCATE TABLE access_points CASCADE;

-- AI & Recommendations
TRUNCATE TABLE ai_recommendations CASCADE;

-- POS & Sales
TRUNCATE TABLE pos_sale_items CASCADE;
TRUNCATE TABLE pos_sales CASCADE;
TRUNCATE TABLE pos_cash_drawers CASCADE;

-- Inventory & Equipment
TRUNCATE TABLE stock_movements CASCADE;
TRUNCATE TABLE maintenance_records CASCADE;
TRUNCATE TABLE maintenance_schedules CASCADE;
TRUNCATE TABLE inventory_items CASCADE;
TRUNCATE TABLE equipment CASCADE;
TRUNCATE TABLE suppliers CASCADE;

-- Newsletters & Campaigns
TRUNCATE TABLE campaign_recipients CASCADE;
TRUNCATE TABLE newsletter_campaigns CASCADE;
TRUNCATE TABLE newsletter_templates CASCADE;

-- Notifications, Suppressions & Webhooks
TRUNCATE TABLE notifications CASCADE;
TRUNCATE TABLE email_suppressions CASCADE;
TRUNCATE TABLE sns_processed_messages CASCADE;
TRUNCATE TABLE stripe_webhook_events CASCADE;

-- Classes & Scheduling
TRUNCATE TABLE class_bookings CASCADE;
TRUNCATE TABLE class_schedules CASCADE;
TRUNCATE TABLE classes CASCADE;
TRUNCATE TABLE class_categories CASCADE;

-- Health & Fitness Tracking
TRUNCATE TABLE workout_exercises CASCADE;
TRUNCATE TABLE workout_logs CASCADE;
TRUNCATE TABLE health_metrics CASCADE;
TRUNCATE TABLE fitness_goals CASCADE;
TRUNCATE TABLE progress_photos CASCADE;
TRUNCATE TABLE wearable_syncs CASCADE;

-- Memberships, Invoices & Payments
TRUNCATE TABLE refund_audit_log CASCADE;
TRUNCATE TABLE payment_refunds CASCADE;
TRUNCATE TABLE refund_requests CASCADE;
TRUNCATE TABLE gym_invoices CASCADE;
TRUNCATE TABLE member_invoices CASCADE;
TRUNCATE TABLE member_payment_methods CASCADE;
TRUNCATE TABLE payment_methods CASCADE;
TRUNCATE TABLE member_memberships CASCADE;
TRUNCATE TABLE membership_plans CASCADE;
TRUNCATE TABLE freeze_policies CASCADE;

-- Subscriptions & Rate Limits
TRUNCATE TABLE subscription_usage CASCADE;
TRUNCATE TABLE subscriptions CASCADE;
TRUNCATE TABLE api_rate_limits CASCADE;

-- Whitelabel Configuration
TRUNCATE TABLE whitelabel_settings CASCADE;

-- Security & Invites
TRUNCATE TABLE user_invites CASCADE;
TRUNCATE TABLE password_reset_tokens CASCADE;
TRUNCATE TABLE token_blacklist CASCADE;
TRUNCATE TABLE pending_registrations CASCADE;

-- Users, Staff, Trainers, Members
TRUNCATE TABLE trainers CASCADE;
TRUNCATE TABLE staff CASCADE;
TRUNCATE TABLE members CASCADE;
TRUNCATE TABLE users CASCADE;

-- Gyms & Organisations
TRUNCATE TABLE gym_areas CASCADE;
TRUNCATE TABLE gyms CASCADE;
TRUNCATE TABLE organisations CASCADE;

-- 3. Re-enable foreign key checks & triggers
SET session_replication_role = 'origin';

-- ============================================================================
-- 4. ENSURE SYSTEM REFERENCE DATA EXISTS
-- ============================================================================

-- Ensure Subscription Tiers
INSERT INTO subscription_tiers (name, display_name, description, price, billing_cycle, max_members, max_locations, max_staff, api_requests_per_hour, features, sort_order, trial_days)
SELECT 'starter', 'Starter', 'Perfect for small gyms getting started', 29.00, 'monthly', 200, 1, 5, 1000,
    '["basic_scheduling", "member_management", "basic_reports"]'::jsonb, 1, 14
WHERE NOT EXISTS (SELECT 1 FROM subscription_tiers WHERE name = 'starter');

INSERT INTO subscription_tiers (name, display_name, description, price, billing_cycle, max_members, max_locations, max_staff, api_requests_per_hour, features, sort_order, trial_days, is_featured)
SELECT 'professional', 'Professional', 'For growing gyms with advanced needs', 79.00, 'monthly', 1000, 3, 25, 5000,
    '["basic_scheduling", "member_management", "advanced_reports", "payment_processing", "class_management", "trainer_management"]'::jsonb, 2, 14, TRUE
WHERE NOT EXISTS (SELECT 1 FROM subscription_tiers WHERE name = 'professional');

INSERT INTO subscription_tiers (name, display_name, description, price, billing_cycle, max_members, max_locations, max_staff, api_requests_per_hour, features, sort_order, trial_days)
SELECT 'enterprise', 'Enterprise', 'Unlimited features for large gym chains', 199.00, 'monthly', 999999, 999, 999, 50000,
    '["basic_scheduling", "member_management", "advanced_reports", "payment_processing", "class_management", "trainer_management", "api_access", "white_label", "priority_support", "custom_integrations"]'::jsonb, 3, 30
WHERE NOT EXISTS (SELECT 1 FROM subscription_tiers WHERE name = 'enterprise');

-- Ensure Exercise Categories
INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Strength', 'Resistance training and weightlifting exercises', 1
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Strength');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Cardio', 'Cardiovascular and aerobic exercises', 2
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Cardio');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Flexibility', 'Stretching and mobility exercises', 3
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Flexibility');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Plyometrics', 'Explosive and jump training', 4
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Plyometrics');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Core', 'Abdominal and core stability exercises', 5
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Core');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Sports', 'Sport-specific training exercises', 6
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Sports');

INSERT INTO exercise_categories (name, description, display_order)
SELECT 'Recovery', 'Recovery and rehabilitation exercises', 7
WHERE NOT EXISTS (SELECT 1 FROM exercise_categories WHERE name = 'Recovery');

COMMIT;

-- ============================================================================
-- 5. VERIFICATION REPORT
-- ============================================================================
DO $$
DECLARE
    org_count INT;
    gym_count INT;
    user_count INT;
    tier_count INT;
    cat_count INT;
BEGIN
    SELECT COUNT(*) INTO org_count FROM organisations;
    SELECT COUNT(*) INTO gym_count FROM gyms;
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO tier_count FROM subscription_tiers;
    SELECT COUNT(*) INTO cat_count FROM exercise_categories;

    RAISE NOTICE '==================================================';
    RAISE NOTICE 'Pre-Go-Live Database Cleanup Successfully Completed';
    RAISE NOTICE '==================================================';
    RAISE NOTICE 'Organisations remaining: %', org_count;
    RAISE NOTICE 'Gyms remaining:          %', gym_count;
    RAISE NOTICE 'Users remaining:         %', user_count;
    RAISE NOTICE 'Subscription Tiers:      % (preserved)', tier_count;
    RAISE NOTICE 'Exercise Categories:     % (preserved)', cat_count;
    RAISE NOTICE '==================================================';
    RAISE NOTICE 'Note: On app restart, Super Admin will auto-init if configured.';
END $$;
