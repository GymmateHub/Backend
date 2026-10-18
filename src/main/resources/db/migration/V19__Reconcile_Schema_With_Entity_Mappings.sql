-- V19: reconcile the Flyway schema with the JPA mappings.
--
-- Until now the Flyway migrations and the entity mappings disagreed (the audit columns
-- is_active / updated_by were never created, created_by was UUID instead of text, several
-- columns and indexes were missing). Environments running ddl-auto=update papered over this;
-- profiles running ddl-auto=validate (dev, docker) could not start.
--
-- Every statement is idempotent so it is safe on databases that Hibernate already patched.

-- api_rate_limits
ALTER TABLE api_rate_limits
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE api_rate_limits SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE api_rate_limits ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- class_bookings
ALTER TABLE class_bookings
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE class_bookings SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE class_bookings ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- class_categories
ALTER TABLE class_categories
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS organisation_id UUID,
    ADD COLUMN IF NOT EXISTS gym_id UUID,
    ADD COLUMN IF NOT EXISTS color VARCHAR(7),
    ADD COLUMN IF NOT EXISTS icon VARCHAR(50);
UPDATE class_categories SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE class_categories ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- class_schedules
ALTER TABLE class_schedules
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE class_schedules SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE class_schedules ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- classes
ALTER TABLE classes
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE classes SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE classes ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- exercise_categories
ALTER TABLE exercise_categories
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE exercise_categories SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE exercise_categories ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- exercises
ALTER TABLE exercises
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE exercises SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE exercises ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- fitness_goals
ALTER TABLE fitness_goals
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE fitness_goals SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE fitness_goals ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- freeze_policies
ALTER TABLE freeze_policies
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE freeze_policies SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE freeze_policies ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- gym_areas
ALTER TABLE gym_areas
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE gym_areas SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE gym_areas ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- gym_invoices
ALTER TABLE gym_invoices
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE gym_invoices SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE gym_invoices ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- gyms
ALTER TABLE gyms
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE gyms SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE gyms ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- health_metrics
ALTER TABLE health_metrics
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE health_metrics SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE health_metrics ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- member_invoices
ALTER TABLE member_invoices
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE member_invoices SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE member_invoices ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- member_memberships
ALTER TABLE member_memberships
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE member_memberships SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE member_memberships ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- member_payment_methods
ALTER TABLE member_payment_methods
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE member_payment_methods SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE member_payment_methods ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- members
ALTER TABLE members
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE members SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE members ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- membership_plans
ALTER TABLE membership_plans
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE membership_plans SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE membership_plans ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- organisations
ALTER TABLE organisations
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE organisations SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE organisations ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- password_reset_tokens
ALTER TABLE password_reset_tokens
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE password_reset_tokens SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE password_reset_tokens ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- payment_methods
ALTER TABLE payment_methods
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS billing_details TEXT,
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS metadata TEXT,
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS wallet_email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS wallet_type VARCHAR(50);
UPDATE payment_methods SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE payment_methods ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- payment_refunds
ALTER TABLE payment_refunds
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS metadata TEXT,
    ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_created_at TIMESTAMP(6);
UPDATE payment_refunds SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE payment_refunds ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- progress_photos
ALTER TABLE progress_photos
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE progress_photos SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE progress_photos ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- refund_requests
ALTER TABLE refund_requests
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS organisation_id UUID,
    ADD COLUMN IF NOT EXISTS due_by TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS escalated BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS escalated_to VARCHAR(50),
    ADD COLUMN IF NOT EXISTS metadata TEXT,
    ADD COLUMN IF NOT EXISTS payment_refund_id UUID;
UPDATE refund_requests SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE refund_requests ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- staff
ALTER TABLE staff
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ALTER COLUMN updated_by TYPE VARCHAR(255) USING updated_by::text;
UPDATE staff SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE staff ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- stripe_webhook_events
ALTER TABLE stripe_webhook_events
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE stripe_webhook_events SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE stripe_webhook_events ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- subscription_tiers
ALTER TABLE subscription_tiers
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- subscription_usage
ALTER TABLE subscription_usage
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE subscription_usage SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE subscription_usage ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- subscriptions
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE subscriptions SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE subscriptions ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- trainers
ALTER TABLE trainers
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ALTER COLUMN updated_by TYPE VARCHAR(255) USING updated_by::text;
UPDATE trainers SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE trainers ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- user_invites
ALTER TABLE user_invites
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE user_invites SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE user_invites ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE users SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE users ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- wearable_syncs
ALTER TABLE wearable_syncs
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE wearable_syncs SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE wearable_syncs ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- workout_exercises
ALTER TABLE workout_exercises
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE workout_exercises SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE workout_exercises ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- workout_logs
ALTER TABLE workout_logs
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT TRUE,
    ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text,
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
UPDATE workout_logs SET is_active = TRUE WHERE is_active IS NULL;
ALTER TABLE workout_logs ALTER COLUMN is_active SET DEFAULT TRUE, ALTER COLUMN is_active SET NOT NULL;

-- member_memberships: the entity mapped plan_id while the schema (and its FK to
-- membership_plans) uses membership_plan_id. Fold any data Hibernate wrote to plan_id back
-- into membership_plan_id, then drop the stray column.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = current_schema() AND table_name = 'member_memberships' AND column_name = 'plan_id') THEN
        UPDATE member_memberships mm
           SET membership_plan_id = mm.plan_id
         WHERE mm.membership_plan_id IS NULL
           AND mm.plan_id IN (SELECT id FROM membership_plans);
        ALTER TABLE member_memberships DROP COLUMN plan_id;
    END IF;
END $$;

-- Indexes declared on the entities
CREATE INDEX IF NOT EXISTS idx_goal_member_status ON fitness_goals (member_id, status);
CREATE INDEX IF NOT EXISTS idx_goal_deadline ON fitness_goals (deadline_date);
CREATE INDEX IF NOT EXISTS idx_gi_organisation ON gym_invoices (organisation_id);
CREATE INDEX IF NOT EXISTS idx_gi_stripe_invoice ON gym_invoices (stripe_invoice_id);
CREATE INDEX IF NOT EXISTS idx_metric_member_type_date ON health_metrics (member_id, metric_type, measurement_date);
CREATE INDEX IF NOT EXISTS idx_metric_gym_date ON health_metrics (gym_id, measurement_date);
CREATE INDEX IF NOT EXISTS idx_notifications_scope_org ON notifications (notification_scope, organisation_id);
CREATE INDEX IF NOT EXISTS idx_pm_owner ON payment_methods (owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_pm_organisation ON payment_methods (organisation_id);
CREATE INDEX IF NOT EXISTS idx_pm_gym ON payment_methods (gym_id);
CREATE INDEX IF NOT EXISTS idx_pm_provider_id ON payment_methods (provider_payment_method_id);
CREATE INDEX IF NOT EXISTS idx_pm_default ON payment_methods (owner_type, owner_id, is_default);
CREATE INDEX IF NOT EXISTS idx_pr_organisation ON payment_refunds (organisation_id);
CREATE INDEX IF NOT EXISTS idx_pr_gym ON payment_refunds (gym_id);
CREATE INDEX IF NOT EXISTS idx_pr_stripe_refund ON payment_refunds (stripe_refund_id);
CREATE INDEX IF NOT EXISTS idx_photo_member_date ON progress_photos (member_id, photo_date);
CREATE INDEX IF NOT EXISTS idx_token ON token_blacklist (token);
CREATE INDEX IF NOT EXISTS idx_expires_at ON token_blacklist (expires_at);
CREATE INDEX IF NOT EXISTS idx_wearable_member_source ON wearable_syncs (member_id, source_type);
CREATE INDEX IF NOT EXISTS idx_workout_exercise_log ON workout_exercises (workout_log_id, exercise_order);
CREATE INDEX IF NOT EXISTS idx_workout_member_date ON workout_logs (member_id, workout_date);
CREATE INDEX IF NOT EXISTS idx_workout_gym ON workout_logs (gym_id, workout_date);
