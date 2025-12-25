-- ============================================
-- GymMate SaaS Subscription & Rate Limiting
-- Migration V1.6
-- ============================================

-- ============================================
-- 1. SUBSCRIPTION TIERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS subscription_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL DEFAULT 'monthly', -- monthly, annual
    is_active BOOLEAN DEFAULT true,
    is_featured BOOLEAN DEFAULT false,

    -- Limits
    max_members INTEGER NOT NULL,
    max_locations INTEGER NOT NULL DEFAULT 1,
    max_staff INTEGER,
    max_classes_per_month INTEGER,

    -- API Rate Limits
    api_requests_per_hour INTEGER NOT NULL DEFAULT 1000,
    api_burst_limit INTEGER NOT NULL DEFAULT 100,
    concurrent_connections INTEGER NOT NULL DEFAULT 10,

    -- Communication Limits
    sms_credits_per_month INTEGER DEFAULT 0,
    email_credits_per_month INTEGER DEFAULT 0,

    -- Feature Flags
    features JSONB DEFAULT '[]'::jsonb,

    -- Overage Pricing
    overage_member_price DECIMAL(10,2) DEFAULT 2.00,
    overage_sms_price DECIMAL(10,2) DEFAULT 0.05,
    overage_email_price DECIMAL(10,2) DEFAULT 0.02,

    -- Metadata
    sort_order INTEGER DEFAULT 0,
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. GYM SUBSCRIPTIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS gym_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    tier_id UUID NOT NULL REFERENCES subscription_tiers(id),

    -- Subscription Details
    status VARCHAR(20) NOT NULL DEFAULT 'active', -- trial, active, past_due, cancelled, expired

    -- Billing
    current_period_start TIMESTAMP NOT NULL,
    current_period_end TIMESTAMP NOT NULL,
    cancel_at_period_end BOOLEAN DEFAULT false,
    cancelled_at TIMESTAMP,
    trial_start TIMESTAMP,
    trial_end TIMESTAMP,

    -- Payment
    stripe_subscription_id VARCHAR(255) UNIQUE,
    stripe_customer_id VARCHAR(255),
    payment_method VARCHAR(50), -- card, bank_account, invoice

    -- Usage Tracking
    current_member_count INTEGER DEFAULT 0,
    current_location_count INTEGER DEFAULT 1,

    -- Metadata
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_gym_active_subscription UNIQUE (gym_id)
);

-- ============================================
-- 3. SUBSCRIPTION USAGE TRACKING TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS subscription_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES gym_subscriptions(id) ON DELETE CASCADE,

    -- Billing Period
    billing_period_start TIMESTAMP NOT NULL,
    billing_period_end TIMESTAMP NOT NULL,

    -- Member Usage
    member_count INTEGER DEFAULT 0,
    member_overage INTEGER DEFAULT 0,

    -- Communication Usage
    sms_sent INTEGER DEFAULT 0,
    sms_overage INTEGER DEFAULT 0,
    email_sent INTEGER DEFAULT 0,
    email_overage INTEGER DEFAULT 0,

    -- API Usage
    api_requests INTEGER DEFAULT 0,
    api_rate_limit_hits INTEGER DEFAULT 0,

    -- Classes
    classes_created INTEGER DEFAULT 0,

    -- Storage (in GB)
    storage_used DECIMAL(10,2) DEFAULT 0,

    -- Calculated Costs
    base_cost DECIMAL(10,2) NOT NULL,
    overage_cost DECIMAL(10,2) DEFAULT 0,
    total_cost DECIMAL(10,2) NOT NULL,

    -- Status
    is_billed BOOLEAN DEFAULT false,
    billed_at TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_subscription_period UNIQUE (subscription_id, billing_period_start)
);

-- ============================================
-- 4. API RATE LIMIT TRACKING TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS api_rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,

    -- Rate Limit Window
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    window_type VARCHAR(20) NOT NULL DEFAULT 'hourly', -- hourly, daily, burst

    -- Request Tracking
    request_count INTEGER DEFAULT 0,
    limit_threshold INTEGER NOT NULL,

    -- Additional Info
    endpoint_path VARCHAR(500),
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Status
    is_blocked BOOLEAN DEFAULT false,
    blocked_until TIMESTAMP,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_gym_window UNIQUE (gym_id, window_start, window_type)
);

-- ============================================
-- 5. PAYMENT METHODS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,

    -- Payment Details
    type VARCHAR(20) NOT NULL, -- card, bank_account, digital_wallet
    provider VARCHAR(50) NOT NULL DEFAULT 'stripe', -- stripe, paypal, etc.
    provider_payment_method_id VARCHAR(255) NOT NULL,

    -- Card/Bank Info (masked)
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    brand VARCHAR(50), -- visa, mastercard, amex, etc.

    -- Status
    is_default BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    verified BOOLEAN DEFAULT false,

    -- Metadata
    billing_details JSONB,
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 6. SUBSCRIPTION INVOICES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS subscription_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES gym_subscriptions(id) ON DELETE CASCADE,
    usage_id UUID REFERENCES subscription_usage(id),

    -- Invoice Details
    invoice_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, open, paid, void, uncollectible

    -- Amounts
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) DEFAULT 0,
    discount DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    amount_paid DECIMAL(10,2) DEFAULT 0,
    amount_due DECIMAL(10,2) NOT NULL,

    -- Currency
    currency VARCHAR(3) DEFAULT 'USD',

    -- Dates
    issue_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,

    -- Payment
    stripe_invoice_id VARCHAR(255) UNIQUE,
    payment_method_id UUID REFERENCES payment_methods(id),

    -- Line Items
    line_items JSONB NOT NULL,

    -- Metadata
    metadata JSONB,
    notes TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 7. SUBSCRIPTION EVENTS/AUDIT TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS subscription_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES gym_subscriptions(id) ON DELETE CASCADE,

    -- Event Details
    event_type VARCHAR(50) NOT NULL, -- created, upgraded, downgraded, cancelled, renewed, payment_failed, etc.
    description TEXT,

    -- Previous and New State
    old_tier_id UUID REFERENCES subscription_tiers(id),
    new_tier_id UUID REFERENCES subscription_tiers(id),
    old_status VARCHAR(20),
    new_status VARCHAR(20),

    -- Actor
    triggered_by UUID, -- user_id who triggered the event
    triggered_by_type VARCHAR(20), -- user, system, admin

    -- Metadata
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Subscription Tiers
CREATE INDEX idx_subscription_tiers_name ON subscription_tiers(name);
CREATE INDEX idx_subscription_tiers_active ON subscription_tiers(is_active);

-- Gym Subscriptions
CREATE INDEX idx_gym_subscriptions_gym_id ON gym_subscriptions(gym_id);
CREATE INDEX idx_gym_subscriptions_tier_id ON gym_subscriptions(tier_id);
CREATE INDEX idx_gym_subscriptions_status ON gym_subscriptions(status);
CREATE INDEX idx_gym_subscriptions_stripe_subscription ON gym_subscriptions(stripe_subscription_id);
CREATE INDEX idx_gym_subscriptions_period ON gym_subscriptions(current_period_start, current_period_end);

-- Subscription Usage
CREATE INDEX idx_subscription_usage_subscription_id ON subscription_usage(subscription_id);
CREATE INDEX idx_subscription_usage_period ON subscription_usage(billing_period_start, billing_period_end);
CREATE INDEX idx_subscription_usage_unbilled ON subscription_usage(is_billed) WHERE is_billed = false;

-- API Rate Limits
CREATE INDEX idx_api_rate_limits_gym_id ON api_rate_limits(gym_id);
CREATE INDEX idx_api_rate_limits_window ON api_rate_limits(window_start, window_end);
CREATE INDEX idx_api_rate_limits_blocked ON api_rate_limits(is_blocked, blocked_until) WHERE is_blocked = true;

-- Payment Methods
CREATE INDEX idx_payment_methods_gym_id ON payment_methods(gym_id);
CREATE INDEX idx_payment_methods_default ON payment_methods(gym_id, is_default) WHERE is_default = true;
CREATE INDEX idx_payment_methods_active ON payment_methods(is_active);

-- Subscription Invoices
CREATE INDEX idx_subscription_invoices_subscription_id ON subscription_invoices(subscription_id);
CREATE INDEX idx_subscription_invoices_status ON subscription_invoices(status);
CREATE INDEX idx_subscription_invoices_due_date ON subscription_invoices(due_date);
CREATE INDEX idx_subscription_invoices_stripe ON subscription_invoices(stripe_invoice_id);

-- Subscription Events
CREATE INDEX idx_subscription_events_subscription_id ON subscription_events(subscription_id);
CREATE INDEX idx_subscription_events_type ON subscription_events(event_type);
CREATE INDEX idx_subscription_events_created_at ON subscription_events(created_at);

-- ============================================
-- SEED DEFAULT SUBSCRIPTION TIERS
-- ============================================

INSERT INTO subscription_tiers (name, display_name, description, price, billing_cycle, max_members, api_requests_per_hour, api_burst_limit, concurrent_connections, sms_credits_per_month, email_credits_per_month, features, sort_order)
VALUES
    -- Starter Plan
    ('starter', 'Starter Plan', 'Perfect for small gyms getting started', 99.00, 'monthly', 200, 1000, 100, 10, 500, 1000,
     '["basic_reporting", "member_check_ins", "basic_class_booking", "email_support"]'::jsonb, 1),

    -- Professional Plan
    ('professional', 'Professional Plan', 'Advanced features for growing gyms', 199.00, 'monthly', 500, 5000, 500, 50, 2000, 5000,
     '["advanced_booking", "payment_processing", "marketing_automation", "ai_insights_limited", "phone_support", "custom_branding"]'::jsonb, 2),

    -- Enterprise Plan
    ('enterprise', 'Enterprise Plan', 'Full-featured solution for large facilities', 399.00, 'monthly', 999999, 25000, 2500, 200, 10000, 25000,
     '["unlimited_members", "full_ai_recommendations", "custom_integrations", "white_label", "dedicated_support", "advanced_analytics", "multi_location"]'::jsonb, 3),

    -- Custom Enterprise
    ('custom', 'Custom Enterprise', 'Tailored solution for multi-location chains', 999.00, 'monthly', 999999, 999999, 10000, 1000, 50000, 100000,
     '["unlimited_everything", "custom_development", "24_7_support", "on_premise_option", "sla_guarantee", "dedicated_account_manager"]'::jsonb, 4)
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- COMMENTS
-- ============================================

COMMENT ON TABLE subscription_tiers IS 'Defines the available SaaS subscription plans/tiers';
COMMENT ON TABLE gym_subscriptions IS 'Tracks active subscriptions for each gym';
COMMENT ON TABLE subscription_usage IS 'Records usage metrics for billing calculations';
COMMENT ON TABLE api_rate_limits IS 'Tracks API rate limit windows and violations';
COMMENT ON TABLE payment_methods IS 'Stores gym payment methods for subscription billing';
COMMENT ON TABLE subscription_invoices IS 'Generated invoices for subscription billing';
COMMENT ON TABLE subscription_events IS 'Audit log of subscription lifecycle events';

