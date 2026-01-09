-- ============================================================================
-- V1_13: Create Health Management Module Tables
-- Description: Creates tables for Exercise Library, Workout Logging,
--              Health Metrics, Fitness Goals, Progress Photos, and Wearable Sync
-- Dependencies: organisations, gyms tables must exist
-- ============================================================================

-- ============================================================================
-- Exercise Categories Table
-- ============================================================================
CREATE TABLE exercise_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    display_order INTEGER,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_exercise_categories_display_order ON exercise_categories(display_order) WHERE active = true;

COMMENT ON TABLE exercise_categories IS 'Exercise categories for organizing exercise library (Strength, Cardio, Flexibility, etc.)';

-- ============================================================================
-- Exercises Table
-- ============================================================================
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category_id UUID REFERENCES exercise_categories(id),
    primary_muscle_group VARCHAR(50),
    secondary_muscle_groups text[],
    equipment_required VARCHAR(100),
    difficulty_level VARCHAR(20),
    instructions JSONB,
    video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT true NOT NULL,
    created_by_gym_id UUID REFERENCES gyms(id),

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_exercise_visibility CHECK (
        (is_public = true AND created_by_gym_id IS NULL) OR
        (is_public = false AND created_by_gym_id IS NOT NULL)
    )
);

CREATE INDEX idx_exercises_category ON exercises(category_id) WHERE active = true;
CREATE INDEX idx_exercises_muscle_group ON exercises(primary_muscle_group) WHERE active = true;
CREATE INDEX idx_exercises_public ON exercises(is_public) WHERE active = true AND is_public = true;
CREATE INDEX idx_exercises_gym ON exercises(created_by_gym_id) WHERE active = true AND created_by_gym_id IS NOT NULL;
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty_level) WHERE active = true;

COMMENT ON TABLE exercises IS 'Exercise library with instructions, categorization, and muscle group targeting';
COMMENT ON COLUMN exercises.is_public IS 'true = available to all gyms, false = gym-specific custom exercise';
COMMENT ON COLUMN exercises.created_by_gym_id IS 'NULL for public exercises, gym ID for custom gym-specific exercises';
COMMENT ON COLUMN exercises.secondary_muscle_groups IS 'Array of additional muscle groups targeted';
COMMENT ON COLUMN exercises.instructions IS 'Step-by-step instructions stored as JSON array';

-- ============================================================================
-- Workout Logs Table
-- ============================================================================
CREATE TABLE workout_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    member_id UUID NOT NULL,
    workout_date TIMESTAMP NOT NULL,
    workout_name VARCHAR(100),
    duration_minutes INTEGER,
    total_calories_burned INTEGER,
    intensity_level VARCHAR(20),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'COMPLETED' NOT NULL,
    recorded_by_user_id UUID,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_workout_duration CHECK (duration_minutes IS NULL OR duration_minutes >= 0),
    CONSTRAINT chk_workout_calories CHECK (total_calories_burned IS NULL OR total_calories_burned >= 0),
    CONSTRAINT chk_workout_intensity CHECK (intensity_level IN ('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH')),
    CONSTRAINT chk_workout_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'SKIPPED'))
);

CREATE INDEX idx_workout_member_date ON workout_logs(member_id, workout_date DESC) WHERE active = true;
CREATE INDEX idx_workout_gym_date ON workout_logs(gym_id, workout_date DESC) WHERE active = true;
CREATE INDEX idx_workout_org_date ON workout_logs(organisation_id, workout_date DESC) WHERE active = true;
CREATE INDEX idx_workout_status ON workout_logs(status) WHERE active = true AND status != 'COMPLETED';

COMMENT ON TABLE workout_logs IS 'Member workout sessions with overall workout metadata';
COMMENT ON COLUMN workout_logs.intensity_level IS 'Overall workout intensity: LOW, MEDIUM, HIGH, VERY_HIGH';
COMMENT ON COLUMN workout_logs.status IS 'Workout status: PLANNED, IN_PROGRESS, COMPLETED, SKIPPED';

-- ============================================================================
-- Workout Exercises Table
-- ============================================================================
CREATE TABLE workout_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_log_id UUID NOT NULL REFERENCES workout_logs(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    exercise_order INTEGER,
    sets INTEGER NOT NULL DEFAULT 1,
    reps INTEGER NOT NULL DEFAULT 1,
    weight DECIMAL(10,2),
    weight_unit VARCHAR(10),
    rest_seconds INTEGER,
    distance_meters DECIMAL(10,2),
    duration_seconds INTEGER,
    notes TEXT,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_workout_exercise_sets CHECK (sets >= 0),
    CONSTRAINT chk_workout_exercise_reps CHECK (reps >= 0),
    CONSTRAINT chk_workout_exercise_weight CHECK (weight IS NULL OR weight >= 0),
    CONSTRAINT chk_workout_exercise_rest CHECK (rest_seconds IS NULL OR rest_seconds >= 0),
    CONSTRAINT chk_workout_exercise_distance CHECK (distance_meters IS NULL OR distance_meters >= 0),
    CONSTRAINT chk_workout_exercise_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0),
    CONSTRAINT chk_workout_exercise_weight_unit CHECK (weight_unit IN ('kg', 'lbs', 'KG', 'LBS') OR weight_unit IS NULL)
);

CREATE INDEX idx_workout_exercise_log_order ON workout_exercises(workout_log_id, exercise_order) WHERE active = true;
CREATE INDEX idx_workout_exercise_id ON workout_exercises(exercise_id) WHERE active = true;

COMMENT ON TABLE workout_exercises IS 'Individual exercises performed within a workout session';
COMMENT ON COLUMN workout_exercises.exercise_order IS 'Order of exercise in workout sequence (1, 2, 3, etc.)';
COMMENT ON COLUMN workout_exercises.distance_meters IS 'Distance for cardio exercises (running, cycling, rowing)';
COMMENT ON COLUMN workout_exercises.duration_seconds IS 'Duration for timed exercises (plank, wall sit)';

-- ============================================================================
-- Health Metrics Table
-- ============================================================================
CREATE TABLE health_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    member_id UUID NOT NULL,
    measurement_date TIMESTAMP NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    notes TEXT,
    recorded_by_user_id UUID,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_health_metric_value CHECK (value > 0),
    CONSTRAINT chk_health_metric_type CHECK (metric_type IN (
        'WEIGHT', 'HEIGHT', 'BODY_FAT_PERCENTAGE', 'MUSCLE_MASS', 'BMI', 'BODY_MASS_INDEX',
        'WAIST_CIRCUMFERENCE', 'CHEST_CIRCUMFERENCE', 'HIP_CIRCUMFERENCE',
        'BICEP_CIRCUMFERENCE', 'THIGH_CIRCUMFERENCE', 'CALF_CIRCUMFERENCE',
        'BLOOD_PRESSURE_SYSTOLIC', 'BLOOD_PRESSURE_DIASTOLIC', 'RESTING_HEART_RATE',
        'VO2_MAX', 'BODY_WATER_PERCENTAGE', 'BONE_MASS', 'VISCERAL_FAT',
        'METABOLIC_AGE'
    ))
);

CREATE INDEX idx_health_metric_member_type_date ON health_metrics(member_id, metric_type, measurement_date DESC) WHERE active = true;
CREATE INDEX idx_health_metric_gym_date ON health_metrics(gym_id, measurement_date DESC) WHERE active = true;
CREATE INDEX idx_health_metric_org_date ON health_metrics(organisation_id, measurement_date DESC) WHERE active = true;
CREATE INDEX idx_health_metric_type ON health_metrics(metric_type) WHERE active = true;

COMMENT ON TABLE health_metrics IS 'Time-series body composition and health measurements';
COMMENT ON COLUMN health_metrics.metric_type IS 'Type of measurement: WEIGHT, BODY_FAT_PERCENTAGE, BMI, circumferences, vital signs, etc.';
COMMENT ON COLUMN health_metrics.unit IS 'Measurement unit: kg, lbs, %, cm, in, bpm, etc.';

-- ============================================================================
-- Fitness Goals Table
-- ============================================================================
CREATE TABLE fitness_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    member_id UUID NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_value DECIMAL(10,2),
    target_unit VARCHAR(20),
    start_value DECIMAL(10,2),
    current_value DECIMAL(10,2),
    start_date DATE NOT NULL,
    deadline_date DATE,
    achieved_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_fitness_goal_dates CHECK (deadline_date IS NULL OR deadline_date >= start_date),
    CONSTRAINT chk_fitness_goal_achieved_date CHECK (achieved_date IS NULL OR achieved_date >= start_date),
    CONSTRAINT chk_fitness_goal_values CHECK (
        target_value IS NULL OR target_value > 0
    ),
    CONSTRAINT chk_fitness_goal_type CHECK (goal_type IN (
        'WEIGHT_LOSS', 'WEIGHT_GAIN', 'MUSCLE_GAIN', 'STRENGTH', 'ENDURANCE',
        'FLEXIBILITY', 'BODY_FAT_REDUCTION', 'CARDIOVASCULAR_FITNESS',
        'ATHLETIC_PERFORMANCE', 'GENERAL_FITNESS', 'REHABILITATION', 'OTHER'
    )),
    CONSTRAINT chk_fitness_goal_status CHECK (status IN ('ACTIVE', 'ACHIEVED', 'ABANDONED', 'ON_HOLD'))
);

CREATE INDEX idx_fitness_goal_member_status ON fitness_goals(member_id, status) WHERE active = true;
CREATE INDEX idx_fitness_goal_deadline ON fitness_goals(deadline_date) WHERE active = true AND status = 'ACTIVE';
CREATE INDEX idx_fitness_goal_gym ON fitness_goals(gym_id, status) WHERE active = true;
CREATE INDEX idx_fitness_goal_org ON fitness_goals(organisation_id, status) WHERE active = true;

COMMENT ON TABLE fitness_goals IS 'Member fitness goals with progress tracking and achievement dates';
COMMENT ON COLUMN fitness_goals.goal_type IS 'Type of goal: WEIGHT_LOSS, MUSCLE_GAIN, STRENGTH, ENDURANCE, etc.';
COMMENT ON COLUMN fitness_goals.status IS 'Goal status: ACTIVE, ACHIEVED, ABANDONED, ON_HOLD';
COMMENT ON COLUMN fitness_goals.current_value IS 'Current progress value, updated as member progresses';

-- ============================================================================
-- Progress Photos Table
-- ============================================================================
CREATE TABLE progress_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    member_id UUID NOT NULL,
    photo_date TIMESTAMP NOT NULL,
    photo_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    weight_at_time DECIMAL(10,2),
    notes TEXT,
    is_public BOOLEAN DEFAULT false NOT NULL,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_progress_photo_weight CHECK (weight_at_time IS NULL OR weight_at_time > 0)
);

CREATE INDEX idx_progress_photo_member_date ON progress_photos(member_id, photo_date DESC) WHERE active = true;
CREATE INDEX idx_progress_photo_gym ON progress_photos(gym_id, photo_date DESC) WHERE active = true;
CREATE INDEX idx_progress_photo_public ON progress_photos(is_public, photo_date DESC) WHERE active = true AND is_public = true;

COMMENT ON TABLE progress_photos IS 'Member progress photos metadata (file upload implementation deferred)';
COMMENT ON COLUMN progress_photos.photo_url IS 'URL to full-size photo (to be implemented with file storage)';
COMMENT ON COLUMN progress_photos.thumbnail_url IS 'URL to thumbnail image';
COMMENT ON COLUMN progress_photos.is_public IS 'Privacy control: false = private to member, true = visible to trainers/gym';

-- ============================================================================
-- Wearable Syncs Table
-- ============================================================================
CREATE TABLE wearable_syncs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL REFERENCES organisations(id),
    gym_id UUID NOT NULL REFERENCES gyms(id),
    member_id UUID NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(20),
    external_user_id VARCHAR(255),
    sync_metadata JSONB,
    sync_error TEXT,

    -- Audit fields
    active BOOLEAN DEFAULT true NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT chk_wearable_source CHECK (source_type IN (
        'APPLE_HEALTH', 'GOOGLE_FIT', 'FITBIT', 'GARMIN', 'SAMSUNG_HEALTH',
        'WHOOP', 'OURA', 'POLAR', 'STRAVA', 'MYFITNESSPAL'
    )),
    CONSTRAINT chk_wearable_status CHECK (sync_status IN ('SUCCESS', 'FAILED', 'PENDING') OR sync_status IS NULL),
    CONSTRAINT uq_member_wearable_source UNIQUE (member_id, source_type)
);

CREATE INDEX idx_wearable_member_source ON wearable_syncs(member_id, source_type) WHERE active = true;
CREATE INDEX idx_wearable_gym ON wearable_syncs(gym_id) WHERE active = true;
CREATE INDEX idx_wearable_last_sync ON wearable_syncs(last_sync_at DESC) WHERE active = true;
CREATE INDEX idx_wearable_status ON wearable_syncs(sync_status) WHERE active = true AND sync_status = 'FAILED';

COMMENT ON TABLE wearable_syncs IS 'Wearable device integration status (OAuth implementation deferred)';
COMMENT ON COLUMN wearable_syncs.source_type IS 'Wearable data source: APPLE_HEALTH, GOOGLE_FIT, FITBIT, etc.';
COMMENT ON COLUMN wearable_syncs.sync_status IS 'Last sync result: SUCCESS, FAILED, PENDING';
COMMENT ON COLUMN wearable_syncs.external_user_id IS 'User ID from external wearable service';
COMMENT ON COLUMN wearable_syncs.sync_metadata IS 'Additional sync information as JSON (scopes, tokens, etc.)';
