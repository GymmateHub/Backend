-- ============================================================================
-- V1: Complete GymMate Database Schema
-- ============================================================================
-- Consolidated migration covering all domain entities for GymMate platform.
-- Generated: 2026-01-11
--
-- This single migration creates the complete database schema including:
-- - Organisation and Multi-tenancy
-- - User Management (Users, Staff, Trainers, Members)
-- - Gym Management
-- - Platform Subscriptions and Rate Limiting
-- - Class Scheduling and Bookings
-- - Membership Plans and Member Subscriptions
-- - Payments and Billing
-- - Health and Fitness Tracking
-- - Security and Authentication
-- ============================================================================

-- ============================================================================
-- SECTION 1: ORGANISATION AND TENANT FOUNDATION
-- ============================================================================

CREATE TABLE IF NOT EXISTS organisations (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    owner_user_id UUID,
    subscription_plan VARCHAR(50) DEFAULT 'starter',
    subscription_status VARCHAR(20) DEFAULT 'trial',
    subscription_started_at TIMESTAMP,
    subscription_expires_at TIMESTAMP,
    trial_ends_at TIMESTAMP,
    max_gyms INTEGER DEFAULT 1,
    max_members INTEGER DEFAULT 200,
    max_staff INTEGER DEFAULT 10,
    billing_email VARCHAR(255),
    billing_address JSONB,
    payment_method_id VARCHAR(255),
    features_enabled JSONB DEFAULT '[]',
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    onboarding_completed BOOLEAN DEFAULT FALSE,
    settings JSONB DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_organisations_slug ON organisations(slug);
CREATE INDEX IF NOT EXISTS idx_organisations_owner ON organisations(owner_user_id);

-- ============================================================================
-- SECTION 2: USER MANAGEMENT
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    profile_photo_url VARCHAR(500),
    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    preferences JSONB DEFAULT '{}',
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    last_login_at TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_organisation ON users(organisation_id);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE TABLE IF NOT EXISTS staff (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    position VARCHAR(100),
    department VARCHAR(50),
    hourly_wage DECIMAL(10, 2),
    hire_date DATE,
    employment_type VARCHAR(20),
    default_schedule JSONB,
    permissions JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_staff_user ON staff(user_id);

CREATE TABLE IF NOT EXISTS trainers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    specializations TEXT[],
    bio TEXT,
    hourly_rate DECIMAL(10, 2),
    commission_rate DECIMAL(5, 2) DEFAULT 0,
    certifications JSONB DEFAULT '[]',
    default_availability JSONB,
    hire_date DATE,
    employment_type VARCHAR(20),
    is_accepting_clients BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_trainers_user ON trainers(user_id);

-- ============================================================================
-- SECTION 3: GYM MANAGEMENT
-- ============================================================================

CREATE TABLE IF NOT EXISTS gyms (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),
    phone VARCHAR(20),
    email VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),
    website VARCHAR(255),
    logo_url VARCHAR(500),
    owner_id UUID,
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(3) DEFAULT 'USD',
    business_hours JSONB,
    subscription_plan VARCHAR(50) DEFAULT 'starter',
    subscription_status VARCHAR(20) DEFAULT 'ACTIVE',
    subscription_expires_at TIMESTAMP,
    max_members INTEGER DEFAULT 200,
    stripe_connect_account_id VARCHAR(255),
    stripe_charges_enabled BOOLEAN DEFAULT FALSE,
    stripe_payouts_enabled BOOLEAN DEFAULT FALSE,
    stripe_details_submitted BOOLEAN DEFAULT FALSE,
    stripe_onboarding_completed_at TIMESTAMP,
    features_enabled JSONB DEFAULT '[]',
    onboarding_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_gyms_organisation ON gyms(organisation_id);
CREATE INDEX IF NOT EXISTS idx_gyms_slug ON gyms(slug);

CREATE TABLE IF NOT EXISTS gym_areas (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    name VARCHAR(100) NOT NULL,
    area_type VARCHAR(50),
    capacity INTEGER,
    amenities TEXT[],
    requires_booking BOOLEAN DEFAULT FALSE,
    advance_booking_hours INTEGER DEFAULT 24,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_gym_areas_gym ON gym_areas(gym_id);

-- ============================================================================
-- SECTION 4: MEMBERS
-- ============================================================================

CREATE TABLE IF NOT EXISTS members (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    user_id UUID NOT NULL,
    membership_number VARCHAR(50) UNIQUE,
    join_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),
    medical_conditions TEXT[],
    allergies TEXT[],
    medications TEXT[],
    fitness_goals TEXT[],
    experience_level VARCHAR(20),
    preferred_workout_times JSONB,
    communication_preferences JSONB,
    waiver_signed BOOLEAN DEFAULT FALSE,
    waiver_signed_date DATE,
    photo_consent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_members_user ON members(user_id);
CREATE INDEX IF NOT EXISTS idx_members_gym ON members(gym_id);
CREATE INDEX IF NOT EXISTS idx_members_organisation ON members(organisation_id);

-- ============================================================================
-- SECTION 5: SUBSCRIPTION AND BILLING (Platform Level)
-- ============================================================================

CREATE TABLE IF NOT EXISTS subscription_tiers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) DEFAULT 'monthly',
    is_active BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    max_members INTEGER NOT NULL,
    max_locations INTEGER DEFAULT 1,
    max_staff INTEGER,
    max_classes_per_month INTEGER,
    api_requests_per_hour INTEGER DEFAULT 1000,
    api_burst_limit INTEGER DEFAULT 100,
    concurrent_connections INTEGER DEFAULT 10,
    sms_credits_per_month INTEGER DEFAULT 0,
    email_credits_per_month INTEGER DEFAULT 0,
    features JSONB DEFAULT '[]',
    overage_member_price DECIMAL(10, 2) DEFAULT 2.00,
    overage_sms_price DECIMAL(10, 2) DEFAULT 0.05,
    overage_email_price DECIMAL(10, 2) DEFAULT 0.02,
    sort_order INTEGER DEFAULT 0,
    metadata JSONB,
    stripe_product_id VARCHAR(255),
    stripe_price_id VARCHAR(255),
    trial_days INTEGER DEFAULT 14,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL UNIQUE,
    tier_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    cancel_at_period_end BOOLEAN DEFAULT FALSE,
    cancelled_at TIMESTAMP,
    trial_start TIMESTAMP,
    trial_end TIMESTAMP,
    stripe_subscription_id VARCHAR(255) UNIQUE,
    stripe_customer_id VARCHAR(255),
    payment_method VARCHAR(50),
    current_member_count INTEGER DEFAULT 0,
    current_location_count INTEGER DEFAULT 1,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_organisation ON subscriptions(organisation_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tier ON subscriptions(tier_id);

CREATE TABLE IF NOT EXISTS subscription_usage (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    subscription_id UUID NOT NULL,
    billing_period_start TIMESTAMP NOT NULL,
    billing_period_end TIMESTAMP NOT NULL,
    member_count INTEGER DEFAULT 0,
    member_overage INTEGER DEFAULT 0,
    sms_sent INTEGER DEFAULT 0,
    sms_overage INTEGER DEFAULT 0,
    email_sent INTEGER DEFAULT 0,
    email_overage INTEGER DEFAULT 0,
    api_requests INTEGER DEFAULT 0,
    api_rate_limit_hits INTEGER DEFAULT 0,
    classes_created INTEGER DEFAULT 0,
    storage_used DECIMAL(10, 2) DEFAULT 0,
    base_cost DECIMAL(10, 2) NOT NULL,
    overage_cost DECIMAL(10, 2) DEFAULT 0,
    total_cost DECIMAL(10, 2) NOT NULL,
    is_billed BOOLEAN DEFAULT FALSE,
    billed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_subscription_usage_subscription ON subscription_usage(subscription_id);

CREATE TABLE IF NOT EXISTS api_rate_limits (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    window_type VARCHAR(20) DEFAULT 'hourly',
    request_count INTEGER DEFAULT 0,
    limit_threshold INTEGER NOT NULL,
    endpoint_path VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_blocked BOOLEAN DEFAULT FALSE,
    blocked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_api_rate_limits_organisation ON api_rate_limits(organisation_id);
CREATE INDEX IF NOT EXISTS idx_api_rate_limits_window ON api_rate_limits(window_start, window_end);

-- ============================================================================
-- SECTION 6: CLASSES AND SCHEDULING
-- ============================================================================

CREATE TABLE IF NOT EXISTS class_categories (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255),
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    category_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    capacity INTEGER DEFAULT 20,
    price DECIMAL(10, 2) DEFAULT 0,
    credits_required INTEGER DEFAULT 1,
    skill_level VARCHAR(20),
    age_restriction VARCHAR(50),
    equipment_needed TEXT[],
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    instructions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_classes_gym ON classes(gym_id);
CREATE INDEX IF NOT EXISTS idx_classes_category ON classes(category_id);

CREATE TABLE IF NOT EXISTS class_schedules (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    class_id UUID NOT NULL,
    trainer_id UUID,
    area_id UUID,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    capacity_override INTEGER,
    price_override DECIMAL(10, 2),
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    cancellation_reason TEXT,
    instructor_notes TEXT,
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_class_schedules_class ON class_schedules(class_id);
CREATE INDEX IF NOT EXISTS idx_class_schedules_gym ON class_schedules(gym_id);
CREATE INDEX IF NOT EXISTS idx_class_schedules_time ON class_schedules(start_time, end_time);

CREATE TABLE IF NOT EXISTS class_bookings (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    class_schedule_id UUID NOT NULL,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    credits_used INTEGER DEFAULT 1,
    amount_paid DECIMAL(10, 2) DEFAULT 0,
    checked_in_at TIMESTAMP,
    checked_out_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    member_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_class_bookings_member ON class_bookings(member_id);
CREATE INDEX IF NOT EXISTS idx_class_bookings_schedule ON class_bookings(class_schedule_id);
CREATE INDEX IF NOT EXISTS idx_class_bookings_gym ON class_bookings(gym_id);

-- ============================================================================
-- SECTION 7: MEMBERSHIP PLANS AND SUBSCRIPTIONS (Gym Level)
-- ============================================================================

CREATE TABLE IF NOT EXISTS membership_plans (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    duration_months INTEGER,
    class_credits INTEGER,
    guest_passes INTEGER DEFAULT 0,
    trainer_sessions INTEGER DEFAULT 0,
    amenities JSONB DEFAULT '[]',
    peak_hours_access BOOLEAN DEFAULT TRUE,
    off_peak_only BOOLEAN DEFAULT FALSE,
    specific_areas JSONB,
    is_featured BOOLEAN DEFAULT FALSE,
    stripe_product_id VARCHAR(255),
    stripe_price_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_membership_plans_gym ON membership_plans(gym_id);

CREATE TABLE IF NOT EXISTS member_memberships (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    membership_plan_id UUID,
    start_date DATE NOT NULL,
    end_date DATE,
    monthly_amount DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    next_billing_date DATE,
    class_credits_remaining INTEGER,
    guest_passes_remaining INTEGER,
    trainer_sessions_remaining INTEGER,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    auto_renew BOOLEAN DEFAULT TRUE,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    is_frozen BOOLEAN DEFAULT FALSE,
    frozen_from DATE,
    frozen_until DATE,
    freeze_reason TEXT,
    total_days_frozen INTEGER DEFAULT 0,
    freeze_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_member_memberships_member ON member_memberships(member_id);
CREATE INDEX IF NOT EXISTS idx_member_memberships_gym ON member_memberships(gym_id);

CREATE TABLE IF NOT EXISTS freeze_policies (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    policy_name VARCHAR(255) NOT NULL,
    max_freeze_days_per_year INTEGER DEFAULT 90,
    max_consecutive_freeze_days INTEGER DEFAULT 60,
    min_membership_days_before_freeze INTEGER DEFAULT 30,
    cooling_off_period_days INTEGER DEFAULT 30,
    freeze_fee_amount DECIMAL(10, 2) DEFAULT 0,
    freeze_fee_frequency VARCHAR(20) DEFAULT 'NONE',
    allow_partial_month_freeze BOOLEAN DEFAULT TRUE,
    is_default_policy BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_freeze_policies_gym ON freeze_policies(gym_id);

CREATE TABLE IF NOT EXISTS member_invoices (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    membership_id UUID,
    stripe_invoice_id VARCHAR(255),
    invoice_number VARCHAR(50),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(30) NOT NULL,
    description TEXT,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    invoice_pdf_url TEXT,
    hosted_invoice_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_member_invoices_member ON member_invoices(member_id);
CREATE INDEX IF NOT EXISTS idx_member_invoices_gym ON member_invoices(gym_id);

CREATE TABLE IF NOT EXISTS member_payment_methods (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    stripe_payment_method_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    card_brand VARCHAR(50),
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_member_payment_methods_member ON member_payment_methods(member_id);

-- ============================================================================
-- SECTION 8: PAYMENT AND BILLING (Platform Level)
-- ============================================================================

CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    owner_type VARCHAR(20) NOT NULL,
    owner_id UUID NOT NULL,
    gym_id UUID,
    member_id UUID,
    provider VARCHAR(50) DEFAULT 'stripe',
    provider_payment_method_id VARCHAR(255) NOT NULL,
    provider_customer_id VARCHAR(255),
    method_type VARCHAR(20) NOT NULL,
    card_brand VARCHAR(50),
    card_last_four VARCHAR(4),
    card_expires_month INTEGER,
    card_expires_year INTEGER,
    bank_name VARCHAR(100),
    bank_last_four VARCHAR(4),
    is_default BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_payment_methods_owner ON payment_methods(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_payment_methods_organisation ON payment_methods(organisation_id);
CREATE INDEX IF NOT EXISTS idx_payment_methods_gym ON payment_methods(gym_id);

CREATE TABLE IF NOT EXISTS gym_invoices (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    stripe_invoice_id VARCHAR(255) UNIQUE,
    invoice_number VARCHAR(50),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(30) NOT NULL,
    description TEXT,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    due_date TIMESTAMP,
    paid_at TIMESTAMP,
    invoice_pdf_url TEXT,
    hosted_invoice_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_gym_invoices_organisation ON gym_invoices(organisation_id);

CREATE TABLE IF NOT EXISTS payment_refunds (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    stripe_refund_id VARCHAR(255) NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255) NOT NULL,
    stripe_charge_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    status VARCHAR(30) NOT NULL,
    reason VARCHAR(50),
    custom_reason TEXT,
    subscription_id UUID,
    invoice_id UUID,
    refund_type VARCHAR(30) DEFAULT 'PLATFORM_SUBSCRIPTION',
    refund_to_user_id UUID,
    refund_to_type VARCHAR(30),
    requested_by UUID,
    requested_by_type VARCHAR(20) DEFAULT 'user',
    processed_by_user_id UUID,
    processed_by_type VARCHAR(30),
    refund_request_id UUID,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_payment_refunds_organisation ON payment_refunds(organisation_id);
CREATE INDEX IF NOT EXISTS idx_payment_refunds_gym ON payment_refunds(gym_id);

CREATE TABLE IF NOT EXISTS refund_requests (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    gym_id UUID NOT NULL,
    refund_type VARCHAR(30) NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    stripe_charge_id VARCHAR(255),
    original_payment_amount DECIMAL(10, 2) NOT NULL,
    requested_refund_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    membership_id UUID,
    class_booking_id UUID,
    subscription_id UUID,
    requested_by_user_id UUID NOT NULL,
    requested_by_type VARCHAR(30) NOT NULL,
    refund_to_user_id UUID NOT NULL,
    refund_to_type VARCHAR(30) NOT NULL,
    reason_category VARCHAR(50) NOT NULL,
    reason_description TEXT,
    supporting_evidence TEXT,
    status VARCHAR(30) DEFAULT 'PENDING',
    processed_by_user_id UUID,
    processed_by_type VARCHAR(30),
    processed_at TIMESTAMP,
    processor_notes TEXT,
    approved_amount DECIMAL(10, 2),
    stripe_refund_id VARCHAR(255),
    rejection_reason TEXT,
    auto_approved BOOLEAN DEFAULT FALSE,
    auto_approval_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_refund_requests_gym ON refund_requests(gym_id);
CREATE INDEX IF NOT EXISTS idx_refund_requests_status ON refund_requests(status);

CREATE TABLE IF NOT EXISTS refund_audit_log (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    refund_request_id UUID,
    payment_refund_id UUID,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    performed_by_user_id UUID,
    performed_by_type VARCHAR(30),
    notes TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refund_audit_log_request ON refund_audit_log(refund_request_id);

CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    stripe_event_id VARCHAR(255) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    payload JSONB,
    error_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_stripe_webhook_events_event_id ON stripe_webhook_events(stripe_event_id);

-- ============================================================================
-- SECTION 9: HEALTH AND FITNESS TRACKING
-- ============================================================================

CREATE TABLE IF NOT EXISTS exercise_categories (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    display_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS exercises (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category_id UUID,
    primary_muscle_group VARCHAR(50),
    secondary_muscle_groups TEXT[],
    equipment_required VARCHAR(100),
    difficulty_level VARCHAR(20),
    instructions JSONB,
    video_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    is_public BOOLEAN DEFAULT TRUE,
    created_by_gym_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_exercises_category ON exercises(category_id);
CREATE INDEX IF NOT EXISTS idx_exercises_muscle ON exercises(primary_muscle_group);

CREATE TABLE IF NOT EXISTS workout_logs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    workout_date TIMESTAMP NOT NULL,
    workout_name VARCHAR(100),
    duration_minutes INTEGER,
    total_calories_burned INTEGER,
    intensity_level VARCHAR(20),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    recorded_by_user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_workout_logs_member_date ON workout_logs(member_id, workout_date);
CREATE INDEX IF NOT EXISTS idx_workout_logs_gym ON workout_logs(gym_id, workout_date);

CREATE TABLE IF NOT EXISTS workout_exercises (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    workout_log_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    exercise_order INTEGER,
    sets INTEGER DEFAULT 1,
    reps INTEGER DEFAULT 1,
    weight DECIMAL(10, 2),
    weight_unit VARCHAR(10),
    rest_seconds INTEGER,
    distance_meters DECIMAL(10, 2),
    duration_seconds INTEGER,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_workout_exercises_log ON workout_exercises(workout_log_id, exercise_order);

CREATE TABLE IF NOT EXISTS health_metrics (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    measurement_date TIMESTAMP NOT NULL,
    metric_type VARCHAR(50) NOT NULL,
    value DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    notes TEXT,
    recorded_by_user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_health_metrics_member_type_date ON health_metrics(member_id, metric_type, measurement_date);
CREATE INDEX IF NOT EXISTS idx_health_metrics_gym_date ON health_metrics(gym_id, measurement_date);

CREATE TABLE IF NOT EXISTS fitness_goals (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    goal_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_value DECIMAL(10, 2),
    target_unit VARCHAR(20),
    start_value DECIMAL(10, 2),
    current_value DECIMAL(10, 2),
    start_date DATE NOT NULL,
    deadline_date DATE,
    achieved_date DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_fitness_goals_member_status ON fitness_goals(member_id, status);
CREATE INDEX IF NOT EXISTS idx_fitness_goals_deadline ON fitness_goals(deadline_date);

CREATE TABLE IF NOT EXISTS progress_photos (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    photo_date TIMESTAMP NOT NULL,
    photo_url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    weight_at_time DECIMAL(10, 2),
    notes TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_progress_photos_member_date ON progress_photos(member_id, photo_date);

CREATE TABLE IF NOT EXISTS wearable_syncs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    last_sync_at TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'PENDING',
    external_user_id VARCHAR(255),
    sync_metadata JSONB,
    sync_error TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_wearable_syncs_member_source ON wearable_syncs(member_id, source_type);

-- ============================================================================
-- SECTION 10: SECURITY AND AUTHENTICATION
-- ============================================================================

CREATE TABLE IF NOT EXISTS pending_registrations (
    registration_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email_verified BOOLEAN DEFAULT FALSE,
    last_otp_sent_at TIMESTAMP,
    otp_attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pending_reg_email ON pending_registrations(email);
CREATE INDEX IF NOT EXISTS idx_pending_reg_expires_at ON pending_registrations(expires_at);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON password_reset_tokens(user_id);

CREATE TABLE IF NOT EXISTS token_blacklist (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    token VARCHAR(1000) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    blacklisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_token_blacklist_token ON token_blacklist(token);
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires_at ON token_blacklist(expires_at);

-- ============================================================================
-- SECTION 11: FOREIGN KEY CONSTRAINTS
-- ============================================================================

DO $$
BEGIN
    -- Organisation references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_users_organisation') THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_gyms_organisation') THEN
        ALTER TABLE gyms ADD CONSTRAINT fk_gyms_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_subscriptions_organisation') THEN
        ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_subscriptions_tier') THEN
        ALTER TABLE subscriptions ADD CONSTRAINT fk_subscriptions_tier FOREIGN KEY (tier_id) REFERENCES subscription_tiers(id);
    END IF;

    -- User references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_staff_user') THEN
        ALTER TABLE staff ADD CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_trainers_user') THEN
        ALTER TABLE trainers ADD CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_members_user') THEN
        ALTER TABLE members ADD CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    -- Gym references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_members_gym') THEN
        ALTER TABLE members ADD CONSTRAINT fk_members_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_gym_areas_gym') THEN
        ALTER TABLE gym_areas ADD CONSTRAINT fk_gym_areas_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_classes_gym') THEN
        ALTER TABLE classes ADD CONSTRAINT fk_classes_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_schedules_gym') THEN
        ALTER TABLE class_schedules ADD CONSTRAINT fk_class_schedules_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_bookings_gym') THEN
        ALTER TABLE class_bookings ADD CONSTRAINT fk_class_bookings_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_membership_plans_gym') THEN
        ALTER TABLE membership_plans ADD CONSTRAINT fk_membership_plans_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_memberships_gym') THEN
        ALTER TABLE member_memberships ADD CONSTRAINT fk_member_memberships_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE SET NULL;
    END IF;

    -- Class references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_schedules_class') THEN
        ALTER TABLE class_schedules ADD CONSTRAINT fk_class_schedules_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_schedules_trainer') THEN
        ALTER TABLE class_schedules ADD CONSTRAINT fk_class_schedules_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_schedules_area') THEN
        ALTER TABLE class_schedules ADD CONSTRAINT fk_class_schedules_area FOREIGN KEY (area_id) REFERENCES gym_areas(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_bookings_member') THEN
        ALTER TABLE class_bookings ADD CONSTRAINT fk_class_bookings_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_class_bookings_schedule') THEN
        ALTER TABLE class_bookings ADD CONSTRAINT fk_class_bookings_schedule FOREIGN KEY (class_schedule_id) REFERENCES class_schedules(id) ON DELETE CASCADE;
    END IF;

    -- Membership references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_memberships_member') THEN
        ALTER TABLE member_memberships ADD CONSTRAINT fk_member_memberships_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_memberships_plan') THEN
        ALTER TABLE member_memberships ADD CONSTRAINT fk_member_memberships_plan FOREIGN KEY (membership_plan_id) REFERENCES membership_plans(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_invoices_member') THEN
        ALTER TABLE member_invoices ADD CONSTRAINT fk_member_invoices_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_member_payment_methods_member') THEN
        ALTER TABLE member_payment_methods ADD CONSTRAINT fk_member_payment_methods_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    -- Health and fitness references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_exercises_category') THEN
        ALTER TABLE exercises ADD CONSTRAINT fk_exercises_category FOREIGN KEY (category_id) REFERENCES exercise_categories(id) ON DELETE SET NULL;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_workout_logs_member') THEN
        ALTER TABLE workout_logs ADD CONSTRAINT fk_workout_logs_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_workout_exercises_log') THEN
        ALTER TABLE workout_exercises ADD CONSTRAINT fk_workout_exercises_log FOREIGN KEY (workout_log_id) REFERENCES workout_logs(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_workout_exercises_exercise') THEN
        ALTER TABLE workout_exercises ADD CONSTRAINT fk_workout_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_health_metrics_member') THEN
        ALTER TABLE health_metrics ADD CONSTRAINT fk_health_metrics_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_fitness_goals_member') THEN
        ALTER TABLE fitness_goals ADD CONSTRAINT fk_fitness_goals_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_progress_photos_member') THEN
        ALTER TABLE progress_photos ADD CONSTRAINT fk_progress_photos_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_wearable_syncs_member') THEN
        ALTER TABLE wearable_syncs ADD CONSTRAINT fk_wearable_syncs_member FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    -- Security references
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_password_reset_tokens_user') THEN
        ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;

    -- Subscription usage
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_subscription_usage_subscription') THEN
        ALTER TABLE subscription_usage ADD CONSTRAINT fk_subscription_usage_subscription FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE;
    END IF;

    -- API rate limits
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_api_rate_limits_organisation') THEN
        ALTER TABLE api_rate_limits ADD CONSTRAINT fk_api_rate_limits_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
    END IF;

    RAISE NOTICE 'Foreign key constraints added successfully';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error adding foreign key constraints: %', SQLERRM;
END;
$$;

-- ============================================================================
-- SECTION 12: SEED DATA
-- ============================================================================

-- Seed subscription tiers
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

-- Seed exercise categories
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

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
-- Tables created: 36
--
-- Organisation and Tenant: organisations
-- User Management: users, staff, trainers
-- Gym Management: gyms, gym_areas
-- Members: members
-- Platform Subscriptions: subscription_tiers, subscriptions, subscription_usage, api_rate_limits
-- Classes: class_categories, classes, class_schedules, class_bookings
-- Gym Memberships: membership_plans, member_memberships, freeze_policies, member_invoices, member_payment_methods
-- Payments: payment_methods, gym_invoices, payment_refunds, refund_requests, refund_audit_log, stripe_webhook_events
-- Health and Fitness: exercise_categories, exercises, workout_logs, workout_exercises, health_metrics, fitness_goals, progress_photos, wearable_syncs
-- Security: pending_registrations, password_reset_tokens, token_blacklist
-- ============================================================================

