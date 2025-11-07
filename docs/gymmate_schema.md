-- =====================================================
-- GymMate Complete Database Schema
-- Comprehensive Gym Management SaaS Platform
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- CORE TENANT & USER MANAGEMENT
-- =====================================================

-- Gyms table (SaaS tenants)
CREATE TABLE gyms (
id UUID PRIMARY KEY,
name VARCHAR(255) NOT NULL,
slug VARCHAR(100) UNIQUE NOT NULL, -- For subdomain/URL routing
address TEXT,
city VARCHAR(100),
state VARCHAR(50),
country VARCHAR(50),
postal_code VARCHAR(20),
phone VARCHAR(20),
email VARCHAR(255),
website VARCHAR(255),
logo_url VARCHAR(500),

    -- Business settings
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(3) DEFAULT 'USD',
    business_hours JSONB, -- {"monday": {"open": "06:00", "close": "22:00"}, ...}

    -- SaaS subscription
    subscription_plan VARCHAR(50) DEFAULT 'starter', -- starter, professional, enterprise
    subscription_status VARCHAR(20) DEFAULT 'active', -- active, suspended, cancelled
    subscription_expires_at TIMESTAMP,
    max_members INTEGER DEFAULT 200,

    -- Features enabled
    features_enabled JSONB DEFAULT '[]'::jsonb, -- ["ai_coaching", "access_control", "pos"]

    -- Status
    is_active BOOLEAN DEFAULT true,
    onboarding_completed BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table (multi-tenant)
CREATE TABLE users (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    -- Authentication
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    email_verified BOOLEAN DEFAULT false,
    email_verification_token VARCHAR(255),
    password_reset_token VARCHAR(255),
    password_reset_expires TIMESTAMP,

    -- Profile
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10), -- male, female, other, prefer_not_to_say
    profile_photo_url VARCHAR(500),

    -- Role & Status
    role VARCHAR(50) NOT NULL DEFAULT 'member', -- admin, trainer, staff, member
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, suspended, banned

    -- Preferences
    preferences JSONB DEFAULT '{}'::jsonb, -- notifications, language, etc.

    -- Security
    two_factor_enabled BOOLEAN DEFAULT false,
    two_factor_secret VARCHAR(255),
    last_login_at TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(gym_id, email)
);

-- =====================================================
-- MEMBERSHIP MANAGEMENT
-- =====================================================

-- Membership plans/types
CREATE TABLE membership_plans (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL, -- monthly, quarterly, yearly, lifetime
    duration_months INTEGER, -- NULL for lifetime

    -- Features
    class_credits INTEGER, -- NULL for unlimited
    guest_passes INTEGER DEFAULT 0,
    trainer_sessions INTEGER DEFAULT 0,
    amenities JSONB DEFAULT '[]'::jsonb, -- ["pool", "sauna", "parking"]

    -- Restrictions
    peak_hours_access BOOLEAN DEFAULT true,
    off_peak_only BOOLEAN DEFAULT false,
    specific_areas JSONB, -- ["main_gym", "pool", "studio"]

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Members
CREATE TABLE members (
id UUID PRIMARY KEY,
user_id UUID REFERENCES users(id) ON DELETE CASCADE,
membership_number VARCHAR(50) UNIQUE,

    -- Member details
    join_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, suspended, cancelled

    -- Emergency contact
    emergency_contact_name VARCHAR(255),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),

    -- Health information
    medical_conditions TEXT[],
    allergies TEXT[],
    medications TEXT[],
    fitness_goals TEXT[],
    experience_level VARCHAR(20), -- beginner, intermediate, advanced

    -- Preferences
    preferred_workout_times JSONB, -- ["morning", "evening"]
    communication_preferences JSONB, -- {"email": true, "sms": false, "push": true}

    -- Waiver & agreements
    waiver_signed BOOLEAN DEFAULT false,
    waiver_signed_date DATE,
    photo_consent BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member memberships (subscription instances)
CREATE TABLE member_memberships (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
membership_plan_id UUID REFERENCES membership_plans(id),

    -- Subscription period
    start_date DATE NOT NULL,
    end_date DATE,

    -- Billing
    monthly_amount DECIMAL(10,2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    next_billing_date DATE,

    -- Usage tracking
    class_credits_remaining INTEGER,
    guest_passes_remaining INTEGER,
    trainer_sessions_remaining INTEGER,

    -- Status
    status VARCHAR(20) DEFAULT 'active', -- active, paused, cancelled, expired
    auto_renew BOOLEAN DEFAULT true,

    -- Freezing/holding
    is_frozen BOOLEAN DEFAULT false,
    frozen_until DATE,
    freeze_reason TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- STAFF & TRAINER MANAGEMENT
-- =====================================================

-- Trainers
CREATE TABLE trainers (
id UUID PRIMARY KEY,
user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    -- Professional info
    specializations TEXT[],
    bio TEXT,
    hourly_rate DECIMAL(10,2),
    commission_rate DECIMAL(5,2) DEFAULT 0.00, -- Percentage

    -- Certifications
    certifications JSONB DEFAULT '[]'::jsonb,
    /* [
        {
            "name": "NASM-CPT",
            "issuer": "NASM",
            "number": "12345",
            "issue_date": "2023-01-01",
            "expiry_date": "2025-01-01"
        }
    ] */

    -- Availability
    default_availability JSONB, -- Weekly schedule template
    /* {
        "monday": [{"start": "09:00", "end": "17:00"}],
        "tuesday": [{"start": "09:00", "end": "17:00"}]
    } */

    -- Employment
    hire_date DATE,
    employment_type VARCHAR(20), -- full_time, part_time, contractor

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_accepting_clients BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Staff (non-trainer employees)
CREATE TABLE staff (
id UUID PRIMARY KEY,
user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    -- Job details
    position VARCHAR(100),
    department VARCHAR(50), -- front_desk, maintenance, management, cleaning
    hourly_wage DECIMAL(10,2),

    -- Employment
    hire_date DATE,
    employment_type VARCHAR(20), -- full_time, part_time, contractor

    -- Schedule
    default_schedule JSONB, -- Weekly work schedule

    -- Permissions
    permissions JSONB DEFAULT '[]'::jsonb, -- ["access_control", "pos", "member_management"]

    -- Status
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CLASSES & SCHEDULING
-- =====================================================

-- Class types/categories
CREATE TABLE class_categories (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7), -- Hex color for UI
    icon VARCHAR(50),

    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Classes
CREATE TABLE classes (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
category_id UUID REFERENCES class_categories(id),

    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    capacity INTEGER DEFAULT 20,

    -- Pricing
    price DECIMAL(10,2) DEFAULT 0.00,
    credits_required INTEGER DEFAULT 1,

    -- Requirements
    skill_level VARCHAR(20), -- beginner, intermediate, advanced, all_levels
    age_restriction VARCHAR(50), -- "18+", "16+", "all_ages"
    equipment_needed TEXT[],

    -- Content
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    instructions TEXT,

    -- Status
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Areas/Rooms in the gym
CREATE TABLE gym_areas (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    area_type VARCHAR(50), -- studio, pool, main_floor, outdoor, virtual
    capacity INTEGER,
    amenities TEXT[],

    -- Booking rules
    requires_booking BOOLEAN DEFAULT false,
    advance_booking_hours INTEGER DEFAULT 24,

    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Class schedules
CREATE TABLE class_schedules (
id UUID PRIMARY KEY,
class_id UUID REFERENCES classes(id) ON DELETE CASCADE,
trainer_id UUID REFERENCES trainers(id),
area_id UUID REFERENCES gym_areas(id),

    -- Timing
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,

    -- Overrides for this specific instance
    capacity_override INTEGER,
    price_override DECIMAL(10,2),

    -- Status
    status VARCHAR(20) DEFAULT 'scheduled', -- scheduled, cancelled, completed, in_progress
    cancellation_reason TEXT,

    -- Notes
    instructor_notes TEXT,
    admin_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Class bookings
CREATE TABLE class_bookings (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
class_schedule_id UUID REFERENCES class_schedules(id) ON DELETE CASCADE,

    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'confirmed', -- confirmed, cancelled, completed, no_show, waitlisted

    -- Payment
    credits_used INTEGER DEFAULT 1,
    amount_paid DECIMAL(10,2) DEFAULT 0.00,

    -- Attendance
    checked_in_at TIMESTAMP,
    checked_out_at TIMESTAMP,

    -- Cancellation
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,

    -- Notes
    member_notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Waitlists for fully booked classes
CREATE TABLE class_waitlists (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
class_schedule_id UUID REFERENCES class_schedules(id) ON DELETE CASCADE,

    position INTEGER NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMP,
    expires_at TIMESTAMP,

    status VARCHAR(20) DEFAULT 'waiting' -- waiting, offered, accepted, expired
);

-- =====================================================
-- PERSONAL TRAINING
-- =====================================================

-- Personal training sessions
CREATE TABLE personal_training_sessions (
id UUID PRIMARY KEY,
trainer_id UUID REFERENCES trainers(id) ON DELETE CASCADE,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
area_id UUID REFERENCES gym_areas(id),

    -- Timing
    scheduled_start TIMESTAMP NOT NULL,
    scheduled_end TIMESTAMP NOT NULL,
    actual_start TIMESTAMP,
    actual_end TIMESTAMP,

    -- Pricing
    rate DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,

    -- Session details
    session_type VARCHAR(50), -- assessment, training, consultation
    focus_areas TEXT[],
    exercises_performed JSONB,

    -- Status
    status VARCHAR(20) DEFAULT 'scheduled', -- scheduled, completed, cancelled, no_show

    -- Notes
    trainer_notes TEXT,
    member_feedback TEXT,
    member_rating INTEGER CHECK (member_rating >= 1 AND member_rating <= 5),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- WORKOUTS & FITNESS TRACKING
-- =====================================================

-- Exercise library
CREATE TABLE exercises (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(255) NOT NULL,
    category VARCHAR(100), -- strength, cardio, flexibility, sports
    muscle_groups TEXT[], -- primary muscle groups targeted
    equipment_needed TEXT[],

    -- Instructions
    description TEXT,
    instructions TEXT,
    safety_tips TEXT,

    -- Media
    image_url VARCHAR(500),
    video_url VARCHAR(500),
    animation_url VARCHAR(500),

    -- Difficulty
    difficulty_level VARCHAR(20), -- beginner, intermediate, advanced

    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Workout templates
CREATE TABLE workout_templates (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
created_by UUID REFERENCES users(id),

    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    difficulty_level VARCHAR(20),
    estimated_duration INTEGER, -- minutes

    -- Template data
    exercises JSONB, -- Structured workout data

    -- Sharing
    is_public BOOLEAN DEFAULT false,
    is_featured BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member workouts (logged sessions)
CREATE TABLE workouts (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
template_id UUID REFERENCES workout_templates(id),

    workout_date DATE NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,

    -- Workout data
    exercises JSONB NOT NULL, -- Actual performed exercises with sets, reps, weights

    -- Metrics
    total_volume DECIMAL(10,2), -- Total weight lifted
    calories_burned INTEGER,

    -- Status
    status VARCHAR(20) DEFAULT 'completed', -- planned, in_progress, completed, skipped

    -- Notes
    notes TEXT,
    mood_rating INTEGER CHECK (mood_rating >= 1 AND mood_rating <= 5),
    energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 5),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- HEALTH METRICS & BODY TRACKING
-- =====================================================

-- Health metrics
CREATE TABLE health_metrics (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,

    recorded_date DATE NOT NULL,

    -- Basic metrics
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    body_fat_percentage DECIMAL(5,2),
    muscle_mass DECIMAL(5,2),
    bone_density DECIMAL(5,2),

    -- Calculated metrics
    bmi DECIMAL(5,2),
    bmr INTEGER, -- Basal metabolic rate

    -- Body measurements (in inches/cm)
    measurements JSONB,
    /* {
        "chest": 40.5,
        "waist": 32.0,
        "hips": 38.0,
        "bicep_left": 15.0,
        "bicep_right": 15.2,
        "thigh_left": 24.0,
        "thigh_right": 24.1
    } */

    -- Vital signs
    resting_heart_rate INTEGER,
    blood_pressure_systolic INTEGER,
    blood_pressure_diastolic INTEGER,

    -- Additional metrics
    water_percentage DECIMAL(5,2),
    visceral_fat_level INTEGER,

    -- Notes
    notes TEXT,
    recorded_by VARCHAR(20) DEFAULT 'member', -- member, trainer, staff

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Progress photos
CREATE TABLE progress_photos (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,

    photo_date DATE NOT NULL,
    photo_url VARCHAR(500) NOT NULL,
    photo_type VARCHAR(20), -- front, side, back, custom

    -- Privacy
    is_private BOOLEAN DEFAULT true,

    -- Notes
    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fitness goals
CREATE TABLE fitness_goals (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,

    goal_type VARCHAR(50), -- weight_loss, weight_gain, strength, endurance, body_composition
    title VARCHAR(255) NOT NULL,
    description TEXT,

    -- Target values
    target_value DECIMAL(10,2),
    target_unit VARCHAR(20),
    current_value DECIMAL(10,2),

    -- Timeline
    target_date DATE,

    -- Status
    status VARCHAR(20) DEFAULT 'active', -- active, achieved, paused, abandoned
    achieved_date DATE,

    -- AI recommendations
    ai_recommendations JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- EQUIPMENT & INVENTORY MANAGEMENT
-- =====================================================

-- Equipment categories
CREATE TABLE equipment_categories (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    description TEXT,
    maintenance_interval_days INTEGER DEFAULT 30,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Equipment
CREATE TABLE equipment (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
category_id UUID REFERENCES equipment_categories(id),
area_id UUID REFERENCES gym_areas(id),

    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),

    -- Purchase info
    purchase_date DATE,
    purchase_price DECIMAL(10,2),
    vendor VARCHAR(255),
    warranty_expires DATE,

    -- Physical details
    dimensions JSONB, -- {"length": 120, "width": 60, "height": 140}
    weight_kg DECIMAL(8,2),
    power_requirements VARCHAR(100),

    -- Status
    status VARCHAR(20) DEFAULT 'operational', -- operational, maintenance, out_of_order, retired
    condition_rating INTEGER CHECK (condition_rating >= 1 AND condition_rating <= 5),

    -- Maintenance
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    maintenance_notes TEXT,

    -- QR code for easy access
    qr_code VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Equipment maintenance logs
CREATE TABLE equipment_maintenance (
id UUID PRIMARY KEY,
equipment_id UUID REFERENCES equipment(id) ON DELETE CASCADE,
performed_by UUID REFERENCES users(id),

    maintenance_date DATE NOT NULL,
    maintenance_type VARCHAR(50), -- routine, repair, inspection, calibration

    -- Work performed
    description TEXT NOT NULL,
    parts_replaced TEXT[],
    cost DECIMAL(10,2),
    vendor VARCHAR(255),

    -- Status change
    status_before VARCHAR(20),
    status_after VARCHAR(20),

    -- Next maintenance
    next_maintenance_date DATE,

    -- Files
    receipt_url VARCHAR(500),
    photos JSONB, -- Array of photo URLs

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory items (supplies, supplements, merchandise)
CREATE TABLE inventory_categories (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    name VARCHAR(100) NOT NULL,
    category_type VARCHAR(50), -- supplies, supplements, merchandise, equipment_parts

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory_items (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
category_id UUID REFERENCES inventory_categories(id),

    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100),
    barcode VARCHAR(100),

    -- Product details
    description TEXT,
    brand VARCHAR(100),
    unit_of_measure VARCHAR(20), -- each, kg, lbs, liters, etc.

    -- Inventory tracking
    current_stock INTEGER DEFAULT 0,
    minimum_stock INTEGER DEFAULT 0,
    maximum_stock INTEGER,
    reorder_point INTEGER,
    reorder_quantity INTEGER,

    -- Costs and pricing
    cost_per_unit DECIMAL(10,2),
    retail_price DECIMAL(10,2),

    -- Suppliers
    primary_supplier VARCHAR(255),
    supplier_sku VARCHAR(100),

    -- Storage
    storage_location VARCHAR(100),
    storage_requirements TEXT,

    -- Expiration tracking
    tracks_expiration BOOLEAN DEFAULT false,
    shelf_life_days INTEGER,

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_sellable BOOLEAN DEFAULT false,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inventory transactions
CREATE TABLE inventory_transactions (
id UUID PRIMARY KEY,
item_id UUID REFERENCES inventory_items(id) ON DELETE CASCADE,
performed_by UUID REFERENCES users(id),

    transaction_type VARCHAR(20), -- stock_in, stock_out, adjustment, transfer, damaged, expired
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(10,2),

    -- Reference
    reference_type VARCHAR(50), -- purchase_order, sale, adjustment, waste
    reference_id VARCHAR(255),

    -- Details
    notes TEXT,
    expiration_date DATE,
    lot_number VARCHAR(100),

    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- FINANCIAL MANAGEMENT
-- =====================================================

-- Payment methods
CREATE TABLE payment_methods (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,

    method_type VARCHAR(20), -- card, bank_account, digital_wallet

    -- Card details (encrypted/tokenized)
    card_token VARCHAR(255),
    card_last_four VARCHAR(4),
    card_brand VARCHAR(20),
    card_expires_month INTEGER,
    card_expires_year INTEGER,

    -- Bank account details (encrypted/tokenized)
    bank_token VARCHAR(255),
    bank_routing_last_four VARCHAR(4),
    bank_account_last_four VARCHAR(4),

    -- Digital wallet
    wallet_type VARCHAR(20), -- paypal, apple_pay, google_pay
    wallet_email VARCHAR(255),

    -- Status
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,

    -- Verification
    is_verified BOOLEAN DEFAULT false,
    verified_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Invoices
CREATE TABLE invoices (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
member_id UUID REFERENCES members(id),

    invoice_number VARCHAR(50) UNIQUE NOT NULL,

    -- Amounts
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,

    -- Dates
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,

    -- Status
    status VARCHAR(20) DEFAULT 'pending', -- pending, paid, overdue, cancelled

    -- Payment tracking
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    paid_date DATE,

    -- Details
    description TEXT,
    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Invoice line items
CREATE TABLE invoice_line_items (
id UUID PRIMARY KEY,
invoice_id UUID REFERENCES invoices(id) ON DELETE CASCADE,

    description VARCHAR(255) NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,

    -- Reference to what was sold
    item_type VARCHAR(50), -- membership, class, training, product, service
    item_id UUID, -- Reference to membership, class, product, etc.

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payments
CREATE TABLE payments (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
member_id UUID REFERENCES members(id),
invoice_id UUID REFERENCES invoices(id),
payment_method_id UUID REFERENCES payment_methods(id),

    -- Payment details
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Payment processing
    payment_processor VARCHAR(50), -- stripe, paypal, square, cash
    processor_transaction_id VARCHAR(255),
    processor_fee DECIMAL(10,2) DEFAULT 0.00,

    -- Status
    status VARCHAR(20) DEFAULT 'pending', -- pending, completed, failed, refunded

    -- Failure details
    failure_reason TEXT,
    failure_code VARCHAR(50),

    -- Notes
    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refunds
CREATE TABLE refunds (
id UUID PRIMARY KEY,
payment_id UUID REFERENCES payments(id) ON DELETE CASCADE,
processed_by UUID REFERENCES users(id),

    refund_amount DECIMAL(10,2) NOT NULL,
    refund_reason TEXT,

    -- Processing
    processor_refund_id VARCHAR(255),
    processor_fee DECIMAL(10,2) DEFAULT 0.00,

    status VARCHAR(20) DEFAULT 'pending', -- pending, completed, failed

    refund_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP
);

-- =====================================================
-- POINT OF SALE (POS)
-- =====================================================

-- POS sales
CREATE TABLE pos_sales (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
member_id UUID REFERENCES members(id), -- Can be null for non-member sales
cashier_id UUID REFERENCES users(id),

    sale_number VARCHAR(50) UNIQUE NOT NULL,

    -- Amounts
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tip_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,

    -- Payment
    payment_method VARCHAR(20), -- cash, card, member_account
    payment_status VARCHAR(20) DEFAULT 'completed',

    -- Customer details (for non-members)
    customer_name VARCHAR(255),
    customer_email VARCHAR(255),
    customer_phone VARCHAR(20),

    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- POS sale items
CREATE TABLE pos_sale_items (
id UUID PRIMARY KEY,
sale_id UUID REFERENCES pos_sales(id) ON DELETE CASCADE,
item_id UUID REFERENCES inventory_items(id),

    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,

    -- Discounts
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    discount_reason VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- ACCESS CONTROL & SECURITY
-- =====================================================

-- Access points (doors, gates, turnstiles)
CREATE TABLE access_points (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
area_id UUID REFERENCES gym_areas(id),

    name VARCHAR(100) NOT NULL,
    device_id VARCHAR(100) UNIQUE,
    device_type VARCHAR(50), -- door, turnstile, gate, locker

    -- Location
    description TEXT,
    is_entry_point BOOLEAN DEFAULT true,
    is_exit_point BOOLEAN DEFAULT true,

    -- Access rules
    requires_membership BOOLEAN DEFAULT true,
    allowed_hours_start TIME,
    allowed_hours_end TIME,
    allowed_days INTEGER[], -- 0=Sunday, 1=Monday, etc.

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_online BOOLEAN DEFAULT false,
    last_heartbeat TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member access cards/fobs
CREATE TABLE member_access_cards (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,

    card_number VARCHAR(100) UNIQUE NOT NULL,
    card_type VARCHAR(20), -- rfid, barcode, mobile, biometric

    -- Status
    is_active BOOLEAN DEFAULT true,
    is_temporary BOOLEAN DEFAULT false,
    expires_at TIMESTAMP,

    -- Lost/stolen
    is_lost BOOLEAN DEFAULT false,
    lost_reported_at TIMESTAMP,

    issued_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Access logs
CREATE TABLE access_logs (
id UUID PRIMARY KEY,
member_id UUID REFERENCES members(id) ON DELETE CASCADE,
access_point_id UUID REFERENCES access_points(id),
card_id UUID REFERENCES member_access_cards(id),

    access_time TIMESTAMP NOT NULL,
    access_type VARCHAR(20), -- entry, exit, denied

    -- Denial reasons
    denial_reason VARCHAR(100), -- inactive_membership, outside_hours, unauthorized_area

    -- Device info
    device_response TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- MARKETING & COMMUNICATIONS
-- =====================================================

-- Marketing campaigns
CREATE TABLE marketing_campaigns (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
created_by UUID REFERENCES users(id),

    name VARCHAR(255) NOT NULL,
    campaign_type VARCHAR(50), -- email, sms, push, social

    -- Content
    subject VARCHAR(255),
    message TEXT,

    -- Targeting
    target_audience JSONB, -- Filters for member selection

    -- Scheduling
    scheduled_for TIMESTAMP,
    sent_at TIMESTAMP,

    -- Status
    status VARCHAR(20) DEFAULT 'draft', -- draft, scheduled, sent, cancelled

    -- Results
    total_recipients INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    opened_count INTEGER DEFAULT 0,
    clicked_count INTEGER DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Communication preferences
CREATE TABLE communication_preferences (
id UUID PRIMARY KEY,
user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    -- Channel preferences
    email_enabled BOOLEAN DEFAULT true,
    sms_enabled BOOLEAN DEFAULT false,
    push_enabled BOOLEAN DEFAULT true,

    -- Content preferences
    marketing_emails BOOLEAN DEFAULT true,
    class_reminders BOOLEAN DEFAULT true,
    payment_notifications BOOLEAN DEFAULT true,
    workout_suggestions BOOLEAN DEFAULT true,

    -- Frequency
    digest_frequency VARCHAR(20) DEFAULT 'weekly', -- daily, weekly, monthly, never

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Automated messages
CREATE TABLE automated_messages (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    trigger_type VARCHAR(50), -- new_member, birthday, payment_due, class_reminder
    message_type VARCHAR(20), -- email, sms, push

    -- Timing
    delay_hours INTEGER DEFAULT 0,
    send_time TIME, -- Preferred time to send

    -- Content
    subject VARCHAR(255),
    message_template TEXT,

    -- Status
    is_active BOOLEAN DEFAULT true,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Message queue
CREATE TABLE message_queue (
id UUID PRIMARY KEY,
user_id UUID REFERENCES users(id) ON DELETE CASCADE,
campaign_id UUID REFERENCES marketing_campaigns(id),
automated_message_id UUID REFERENCES automated_messages(id),

    message_type VARCHAR(20), -- email, sms, push
    recipient VARCHAR(255), -- email address or phone number

    -- Content
    subject VARCHAR(255),
    message TEXT,

    -- Scheduling
    scheduled_for TIMESTAMP NOT NULL,

    -- Status
    status VARCHAR(20) DEFAULT 'pending', -- pending, sent, failed, cancelled
    sent_at TIMESTAMP,

    -- Results
    delivered BOOLEAN DEFAULT false,
    opened BOOLEAN DEFAULT false,
    clicked BOOLEAN DEFAULT false,

    -- Error handling
    attempt_count INTEGER DEFAULT 0,
    last_error TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- REPORTS & ANALYTICS
-- =====================================================

-- Analytics events
CREATE TABLE analytics_events (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
user_id UUID REFERENCES users(id),

    event_type VARCHAR(50), -- page_view, button_click, class_booking, workout_completed
    event_name VARCHAR(100),

    -- Context
    page_url VARCHAR(500),
    user_agent TEXT,
    ip_address INET,

    -- Custom properties
    properties JSONB,

    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- SYSTEM & CONFIGURATION
-- =====================================================

-- System settings per gym
CREATE TABLE gym_settings (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,

    setting_key VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(20) DEFAULT 'string', -- string, number, boolean, json

    -- Metadata
    description TEXT,
    is_public BOOLEAN DEFAULT false,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(gym_id, setting_key)
);

-- Audit logs
CREATE TABLE audit_logs (
id UUID PRIMARY KEY,
gym_id UUID REFERENCES gyms(id) ON DELETE CASCADE,
user_id UUID REFERENCES users(id),

    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50),
    resource_id UUID,

    -- Changes
    old_values JSONB,
    new_values JSONB,

    -- Context
    ip_address INET,
    user_agent TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- User indexes
CREATE INDEX idx_users_gym_id ON users(gym_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE UNIQUE INDEX idx_users_gym_email ON users(gym_id, email);

-- Member indexes
CREATE INDEX idx_members_user_id ON members(user_id);
CREATE INDEX idx_members_status ON members(status);
CREATE INDEX idx_member_memberships_member_id ON member_memberships(member_id);
CREATE INDEX idx_member_memberships_status ON member_memberships(status);
CREATE INDEX idx_member_memberships_dates ON member_memberships(start_date, end_date);

-- Class and booking indexes
CREATE INDEX idx_class_schedules_start_time ON class_schedules(start_time);
CREATE INDEX idx_class_schedules_trainer ON class_schedules(trainer_id);
CREATE INDEX idx_class_schedules_class ON class_schedules(class_id);
CREATE INDEX idx_class_bookings_member ON class_bookings(member_id);
CREATE INDEX idx_class_bookings_schedule ON class_bookings(class_schedule_id);
CREATE INDEX idx_class_bookings_status ON class_bookings(status);

-- Workout and health indexes
CREATE INDEX idx_workouts_member_date ON workouts(member_id, workout_date);
CREATE INDEX idx_health_metrics_member_date ON health_metrics(member_id, recorded_date);

-- Financial indexes
CREATE INDEX idx_payments_member ON payments(member_id);
CREATE INDEX idx_payments_date ON payments(payment_date);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_invoices_member ON invoices(member_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);

-- Access control indexes
CREATE INDEX idx_access_logs_member ON access_logs(member_id);
CREATE INDEX idx_access_logs_time ON access_logs(access_time);
CREATE INDEX idx_access_logs_point ON access_logs(access_point_id);

-- Equipment indexes
CREATE INDEX idx_equipment_gym ON equipment(gym_id);
CREATE INDEX idx_equipment_status ON equipment(status);
CREATE INDEX idx_equipment_maintenance_date ON equipment(next_maintenance_date);

-- Inventory indexes
CREATE INDEX idx_inventory_items_gym ON inventory_items(gym_id);
CREATE INDEX idx_inventory_items_stock ON inventory_items(current_stock);
CREATE INDEX idx_inventory_transactions_item ON inventory_transactions(item_id);
CREATE INDEX idx_inventory_transactions_date ON inventory_transactions(transaction_date);

-- =====================================================
-- TRIGGERS FOR AUTOMATIC UPDATES
-- =====================================================

-- Update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply update triggers to relevant tables
CREATE TRIGGER update_gyms_updated_at BEFORE UPDATE ON gyms FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_members_updated_at BEFORE UPDATE ON members FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_trainers_updated_at BEFORE UPDATE ON trainers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_classes_updated_at BEFORE UPDATE ON classes FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_class_schedules_updated_at BEFORE UPDATE ON class_schedules FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_equipment_updated_at BEFORE UPDATE ON equipment FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_workouts_updated_at BEFORE UPDATE ON workouts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA INSERTION (Optional)
-- =====================================================

-- This section would contain INSERT statements for sample data
-- Commented out for production use

/*
-- Sample gym
INSERT INTO gyms (name, slug, address, city, state, country, email, phone)
VALUES ('FitLife Gym', 'fitlife-gym', '123 Fitness Ave', 'Los Angeles', 'CA', 'USA', 'info@fitlifegym.com', '555-0123');

-- Sample admin user
INSERT INTO users (gym_id, email, password_hash, first_name, last_name, role)
VALUES (
(SELECT id FROM gyms WHERE slug = 'fitlife-gym'),
'admin@fitlifegym.com',
'$2b$10$example_hash',
'Admin',
'User',
'admin'
);
*/
