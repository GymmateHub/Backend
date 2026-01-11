-- ============================================================================
-- V1_15: Complete Database Schema Alignment (Development Reset)
-- ============================================================================
-- This migration ensures complete alignment between Java entities and PostgreSQL
-- tables. It drops and recreates tables where schema mismatches exist.
--
-- WARNING: This is for DEVELOPMENT environments only.
-- This migration WILL DELETE existing data in affected tables.
--
-- Tables affected:
-- - exercise_categories (recreate)
-- - exercises (recreate to match entity)
-- - workout_logs (recreate to match entity)
-- - workout_exercises (recreate to match entity)
-- - health_metrics (recreate - normalized design)
-- - fitness_goals (recreate to match entity)
-- - progress_photos (recreate to match entity)
-- - wearable_syncs (create if not exists)
-- - member_memberships (cleanup duplicate column)
-- ============================================================================

-- ============================================================================
-- SECTION 1: Drop existing tables in dependency order
-- ============================================================================

DROP TABLE IF EXISTS workout_exercises CASCADE;
DROP TABLE IF EXISTS workout_logs CASCADE;
DROP TABLE IF EXISTS wearable_syncs CASCADE;
DROP TABLE IF EXISTS progress_photos CASCADE;
DROP TABLE IF EXISTS fitness_goals CASCADE;
DROP TABLE IF EXISTS health_metrics CASCADE;
DROP TABLE IF EXISTS exercises CASCADE;
DROP TABLE IF EXISTS exercise_categories CASCADE;

-- ============================================================================
-- SECTION 2: Create exercise_categories table
-- Matches: ExerciseCategory.java extends BaseAuditEntity
-- ============================================================================

CREATE TABLE exercise_categories (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Entity fields
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    display_order INTEGER,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL
);

CREATE INDEX idx_exercise_categories_active ON exercise_categories(is_active) WHERE is_active = true;
CREATE INDEX idx_exercise_categories_order ON exercise_categories(display_order);

COMMENT ON TABLE exercise_categories IS 'Exercise categories (Strength, Cardio, Flexibility, etc.)';

-- ============================================================================
-- SECTION 3: Create exercises table
-- Matches: Exercise.java extends BaseAuditEntity
-- ============================================================================

CREATE TABLE exercises (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Entity fields
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES exercise_categories(id),
    primary_muscle_group VARCHAR(50),
    secondary_muscle_groups TEXT[],
    equipment_required VARCHAR(100),
    difficulty_level VARCHAR(20),
    instructions JSONB,
    video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT true NOT NULL,
    created_by_gym_id UUID REFERENCES gyms(id),

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL
);

CREATE INDEX idx_exercises_category ON exercises(category_id) WHERE is_active = true;
CREATE INDEX idx_exercises_muscle ON exercises(primary_muscle_group) WHERE is_active = true;
CREATE INDEX idx_exercises_public ON exercises(is_public) WHERE is_active = true AND is_public = true;
CREATE INDEX idx_exercises_gym ON exercises(created_by_gym_id) WHERE is_active = true AND created_by_gym_id IS NOT NULL;
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty_level) WHERE is_active = true;

COMMENT ON TABLE exercises IS 'Exercise library with instructions and categorization';
COMMENT ON COLUMN exercises.is_public IS 'true = public library, false = gym-specific custom exercise';
COMMENT ON COLUMN exercises.instructions IS 'Step-by-step instructions as JSON array';

-- ============================================================================
-- SECTION 4: Create workout_logs table
-- Matches: WorkoutLog.java extends GymScopedEntity
-- ============================================================================

CREATE TABLE workout_logs (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- TenantEntity fields (from GymScopedEntity parent)
    organisation_id UUID NOT NULL REFERENCES organisations(id),

    -- GymScopedEntity fields
    gym_id UUID NOT NULL REFERENCES gyms(id),

    -- Entity fields
    member_id UUID NOT NULL,
    workout_date TIMESTAMP NOT NULL,
    workout_name VARCHAR(100),
    duration_minutes INTEGER,
    total_calories_burned INTEGER,
    intensity_level VARCHAR(20), -- Enum: LOW, MEDIUM, HIGH, VERY_HIGH
    notes TEXT,
    status VARCHAR(20) DEFAULT 'COMPLETED' NOT NULL, -- Enum: PLANNED, IN_PROGRESS, COMPLETED, SKIPPED
    recorded_by_user_id UUID,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL,

    -- Constraints
    CONSTRAINT chk_workout_duration CHECK (duration_minutes IS NULL OR duration_minutes >= 0),
    CONSTRAINT chk_workout_calories CHECK (total_calories_burned IS NULL OR total_calories_burned >= 0),
    CONSTRAINT chk_workout_intensity CHECK (intensity_level IS NULL OR intensity_level IN ('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH')),
    CONSTRAINT chk_workout_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED'))
);

CREATE INDEX idx_workout_logs_member_date ON workout_logs(member_id, workout_date DESC) WHERE is_active = true;
CREATE INDEX idx_workout_logs_gym_date ON workout_logs(gym_id, workout_date DESC) WHERE is_active = true;
CREATE INDEX idx_workout_logs_org ON workout_logs(organisation_id) WHERE is_active = true;
CREATE INDEX idx_workout_logs_status ON workout_logs(status) WHERE is_active = true;

COMMENT ON TABLE workout_logs IS 'Member workout sessions with overall metadata';

-- ============================================================================
-- SECTION 5: Create workout_exercises table
-- Matches: WorkoutExercise.java extends BaseAuditEntity
-- ============================================================================

CREATE TABLE workout_exercises (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Entity fields
    workout_log_id UUID NOT NULL REFERENCES workout_logs(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    exercise_order INTEGER,
    sets INTEGER NOT NULL DEFAULT 1,
    reps INTEGER NOT NULL DEFAULT 1,
    weight DECIMAL(10, 2),
    weight_unit VARCHAR(10), -- kg, lbs
    rest_seconds INTEGER,
    distance_meters DECIMAL(10, 2),
    duration_seconds INTEGER,
    notes TEXT,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL,

    -- Constraints
    CONSTRAINT chk_workout_exercise_sets CHECK (sets >= 0),
    CONSTRAINT chk_workout_exercise_reps CHECK (reps >= 0),
    CONSTRAINT chk_workout_exercise_weight CHECK (weight IS NULL OR weight >= 0),
    CONSTRAINT chk_workout_exercise_rest CHECK (rest_seconds IS NULL OR rest_seconds >= 0),
    CONSTRAINT chk_workout_exercise_distance CHECK (distance_meters IS NULL OR distance_meters >= 0),
    CONSTRAINT chk_workout_exercise_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    CONSTRAINT chk_workout_exercise_weight_unit CHECK (weight_unit IS NULL OR weight_unit IN ('kg', 'lbs', 'KG', 'LBS'))
);

CREATE INDEX idx_workout_exercises_log_order ON workout_exercises(workout_log_id, exercise_order) WHERE is_active = true;
CREATE INDEX idx_workout_exercises_exercise ON workout_exercises(exercise_id) WHERE is_active = true;

COMMENT ON TABLE workout_exercises IS 'Individual exercises within a workout session';

-- ============================================================================
-- SECTION 6: Create health_metrics table (NORMALIZED design)
-- Matches: HealthMetric.java extends GymScopedEntity
-- ============================================================================

CREATE TABLE health_metrics (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- TenantEntity fields (from GymScopedEntity parent)
    organisation_id UUID NOT NULL REFERENCES organisations(id),

    -- GymScopedEntity fields
    gym_id UUID NOT NULL REFERENCES gyms(id),

    -- Entity fields (NORMALIZED - one row per metric)
    member_id UUID NOT NULL,
    measurement_date TIMESTAMP NOT NULL,
    metric_type VARCHAR(50) NOT NULL, -- Enum: WEIGHT, HEIGHT, BODY_FAT_PERCENTAGE, etc.
    value DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(10) NOT NULL, -- kg, lbs, %, cm, bpm, etc.
    notes TEXT,
    recorded_by_user_id UUID,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL,

    -- Constraints
    CONSTRAINT chk_health_metric_value CHECK (value > 0),
    CONSTRAINT chk_health_metric_type CHECK (metric_type IN (
        'WEIGHT', 'HEIGHT', 'BODY_FAT_PERCENTAGE', 'MUSCLE_MASS', 'BMI', 'BODY_MASS_INDEX',
        'WAIST_CIRCUMFERENCE', 'CHEST_CIRCUMFERENCE', 'HIP_CIRCUMFERENCE',
        'BICEP_CIRCUMFERENCE', 'THIGH_CIRCUMFERENCE', 'NECK_CIRCUMFERENCE', 'CALF_CIRCUMFERENCE',
        'BLOOD_PRESSURE_SYSTOLIC', 'BLOOD_PRESSURE_DIASTOLIC', 'RESTING_HEART_RATE', 'MAX_HEART_RATE',
        'VO2_MAX', 'ONE_REP_MAX_BENCH', 'ONE_REP_MAX_SQUAT', 'ONE_REP_MAX_DEADLIFT'
    ))
);

CREATE INDEX idx_health_metrics_member_type_date ON health_metrics(member_id, metric_type, measurement_date DESC) WHERE is_active = true;
CREATE INDEX idx_health_metrics_gym ON health_metrics(gym_id, measurement_date DESC) WHERE is_active = true;
CREATE INDEX idx_health_metrics_org ON health_metrics(organisation_id) WHERE is_active = true;
CREATE INDEX idx_health_metrics_type ON health_metrics(metric_type) WHERE is_active = true;

COMMENT ON TABLE health_metrics IS 'Time-series body composition and health measurements (normalized - one metric per row)';
COMMENT ON COLUMN health_metrics.metric_type IS 'Type of measurement from MetricType enum';
COMMENT ON COLUMN health_metrics.unit IS 'Measurement unit: kg, lbs, %, cm, in, bpm, etc.';

-- ============================================================================
-- SECTION 7: Create fitness_goals table
-- Matches: FitnessGoal.java extends GymScopedEntity
-- ============================================================================

CREATE TABLE fitness_goals (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- TenantEntity fields (from GymScopedEntity parent)
    organisation_id UUID NOT NULL REFERENCES organisations(id),

    -- GymScopedEntity fields
    gym_id UUID NOT NULL REFERENCES gyms(id),

    -- Entity fields
    member_id UUID NOT NULL,
    goal_type VARCHAR(50) NOT NULL, -- Enum: WEIGHT_LOSS, MUSCLE_GAIN, STRENGTH, etc.
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_value DECIMAL(10, 2),
    target_unit VARCHAR(20),
    start_value DECIMAL(10, 2),
    current_value DECIMAL(10, 2),
    start_date DATE NOT NULL,
    deadline_date DATE,
    achieved_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL, -- Enum: ACTIVE, ACHIEVED, ABANDONED, ON_HOLD

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL,

    -- Constraints
    CONSTRAINT chk_fitness_goal_dates CHECK (deadline_date IS NULL OR deadline_date >= start_date),
    CONSTRAINT chk_fitness_goal_achieved CHECK (achieved_date IS NULL OR achieved_date >= start_date),
    CONSTRAINT chk_fitness_goal_values CHECK (target_value IS NULL OR target_value > 0),
    CONSTRAINT chk_fitness_goal_type CHECK (goal_type IN (
        'WEIGHT_LOSS', 'MUSCLE_GAIN', 'STRENGTH', 'ENDURANCE', 'FLEXIBILITY',
        'BODY_FAT_REDUCTION', 'GENERAL_FITNESS', 'SPECIFIC_EXERCISE',
        'CONSISTENCY', 'COMPETITION_PREP'
    )),
    CONSTRAINT chk_fitness_goal_status CHECK (status IN ('ACTIVE', 'ACHIEVED', 'ABANDONED', 'ON_HOLD'))
);

CREATE INDEX idx_fitness_goals_member_status ON fitness_goals(member_id, status) WHERE is_active = true;
CREATE INDEX idx_fitness_goals_deadline ON fitness_goals(deadline_date) WHERE is_active = true AND status = 'ACTIVE';
CREATE INDEX idx_fitness_goals_gym ON fitness_goals(gym_id) WHERE is_active = true;
CREATE INDEX idx_fitness_goals_org ON fitness_goals(organisation_id) WHERE is_active = true;

COMMENT ON TABLE fitness_goals IS 'Member fitness goals with progress tracking';
COMMENT ON COLUMN fitness_goals.goal_type IS 'Type from GoalType enum';
COMMENT ON COLUMN fitness_goals.status IS 'Status from GoalStatus enum';

-- ============================================================================
-- SECTION 8: Create progress_photos table
-- Matches: ProgressPhoto.java extends GymScopedEntity
-- ============================================================================

CREATE TABLE progress_photos (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- TenantEntity fields (from GymScopedEntity parent)
    organisation_id UUID NOT NULL REFERENCES organisations(id),

    -- GymScopedEntity fields
    gym_id UUID NOT NULL REFERENCES gyms(id),

    -- Entity fields
    member_id UUID NOT NULL,
    photo_date TIMESTAMP NOT NULL,
    photo_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    weight_at_time DECIMAL(10, 2),
    notes TEXT,
    is_public BOOLEAN DEFAULT false NOT NULL,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL
);

CREATE INDEX idx_progress_photos_member_date ON progress_photos(member_id, photo_date DESC) WHERE is_active = true;
CREATE INDEX idx_progress_photos_gym ON progress_photos(gym_id) WHERE is_active = true;
CREATE INDEX idx_progress_photos_org ON progress_photos(organisation_id) WHERE is_active = true;

COMMENT ON TABLE progress_photos IS 'Member progress photos for tracking transformations';

-- ============================================================================
-- SECTION 9: Create wearable_syncs table
-- Matches: WearableSync.java extends GymScopedEntity
-- ============================================================================

CREATE TABLE wearable_syncs (
    -- BaseEntity fields
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- TenantEntity fields (from GymScopedEntity parent)
    organisation_id UUID NOT NULL REFERENCES organisations(id),

    -- GymScopedEntity fields
    gym_id UUID NOT NULL REFERENCES gyms(id),

    -- Entity fields
    member_id UUID NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- Enum: APPLE_HEALTH, GOOGLE_FIT, FITBIT, GARMIN, etc.
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'PENDING', -- SUCCESS, FAILED, PENDING
    external_user_id VARCHAR(255),
    sync_metadata JSONB,
    sync_error TEXT,

    -- BaseAuditEntity fields
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true NOT NULL
);

CREATE INDEX idx_wearable_syncs_member_source ON wearable_syncs(member_id, source_type) WHERE is_active = true;
CREATE INDEX idx_wearable_syncs_gym ON wearable_syncs(gym_id) WHERE is_active = true;
CREATE INDEX idx_wearable_syncs_status ON wearable_syncs(sync_status) WHERE is_active = true;

COMMENT ON TABLE wearable_syncs IS 'Wearable device integration status (Apple Health, Google Fit, etc.)';

-- ============================================================================
-- SECTION 10: Fix member_memberships duplicate column
-- ============================================================================

-- Migrate data from plan_id to membership_plan_id if needed
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'member_memberships' AND column_name = 'plan_id'
    ) THEN
        -- Copy data if membership_plan_id is null
        UPDATE member_memberships
        SET membership_plan_id = plan_id
        WHERE membership_plan_id IS NULL AND plan_id IS NOT NULL;

        -- Drop the duplicate column
        ALTER TABLE member_memberships DROP COLUMN IF EXISTS plan_id;

        RAISE NOTICE 'Removed duplicate plan_id column from member_memberships';
    END IF;
END $$;

-- ============================================================================
-- SECTION 11: Ensure all audit columns are consistent
-- ============================================================================

-- Add is_active column to any tables that might be missing it
DO $$
DECLARE
    tbl TEXT;
    tables_to_check TEXT[] := ARRAY[
        'users', 'gyms', 'organisations', 'members', 'membership_plans',
        'member_memberships', 'trainers', 'staff', 'classes', 'class_schedules',
        'class_bookings', 'class_categories', 'gym_areas', 'subscriptions',
        'subscription_tiers', 'payment_methods', 'payment_refunds', 'refund_requests'
    ];
BEGIN
    FOREACH tbl IN ARRAY tables_to_check
    LOOP
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = tbl AND table_schema = 'public') THEN
            IF NOT EXISTS (
                SELECT 1 FROM information_schema.columns
                WHERE table_name = tbl AND column_name = 'is_active' AND table_schema = 'public'
            ) THEN
                EXECUTE format('ALTER TABLE %I ADD COLUMN is_active BOOLEAN DEFAULT true', tbl);
                RAISE NOTICE 'Added is_active column to %', tbl;
            END IF;
        END IF;
    END LOOP;
END $$;

-- ============================================================================
-- SECTION 12: Verify and log completion
-- ============================================================================

DO $$
BEGIN
    RAISE NOTICE '============================================';
    RAISE NOTICE 'V1_15 Schema Alignment Complete';
    RAISE NOTICE '============================================';
    RAISE NOTICE 'Tables created/recreated:';
    RAISE NOTICE '  - exercise_categories';
    RAISE NOTICE '  - exercises';
    RAISE NOTICE '  - workout_logs';
    RAISE NOTICE '  - workout_exercises';
    RAISE NOTICE '  - health_metrics (normalized)';
    RAISE NOTICE '  - fitness_goals';
    RAISE NOTICE '  - progress_photos';
    RAISE NOTICE '  - wearable_syncs';
    RAISE NOTICE '';
    RAISE NOTICE 'Cleanups performed:';
    RAISE NOTICE '  - member_memberships.plan_id removed (duplicate)';
    RAISE NOTICE '  - is_active column ensured on all tables';
    RAISE NOTICE '============================================';
END $$;

