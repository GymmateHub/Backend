-- ============================================
-- V1_7: Add Stripe Payment Integration Tables
-- ============================================

-- Add Stripe Connect columns to gyms table
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS stripe_connect_account_id VARCHAR(255);
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS stripe_charges_enabled BOOLEAN DEFAULT false;
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS stripe_payouts_enabled BOOLEAN DEFAULT false;
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS stripe_details_submitted BOOLEAN DEFAULT false;
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS stripe_onboarding_completed_at TIMESTAMP;

-- Add Stripe columns to subscription_tiers table (for platform pricing)
ALTER TABLE subscription_tiers ADD COLUMN IF NOT EXISTS stripe_product_id VARCHAR(255);
ALTER TABLE subscription_tiers ADD COLUMN IF NOT EXISTS stripe_price_id VARCHAR(255);
ALTER TABLE subscription_tiers ADD COLUMN IF NOT EXISTS trial_days INTEGER DEFAULT 14;

-- Add Stripe columns to membership_plans table
ALTER TABLE membership_plans ADD COLUMN IF NOT EXISTS stripe_product_id VARCHAR(255);
ALTER TABLE membership_plans ADD COLUMN IF NOT EXISTS stripe_price_id VARCHAR(255);

-- Add Stripe columns to member_memberships table
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS stripe_customer_id VARCHAR(255);
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS stripe_subscription_id VARCHAR(255);

-- Create gym_payment_methods table for platform payments (Gym → GymMate)
CREATE TABLE IF NOT EXISTS gym_payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    stripe_payment_method_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    card_brand VARCHAR(50),
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_gym_payment_methods_gym_id ON gym_payment_methods(gym_id);

-- Create member_payment_methods table for member payments (Member → Gym)
CREATE TABLE IF NOT EXISTS member_payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL,
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    stripe_payment_method_id VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    card_brand VARCHAR(50),
    last_four VARCHAR(4),
    expiry_month INTEGER,
    expiry_year INTEGER,
    is_default BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_member_payment_methods_member_id ON member_payment_methods(member_id);
CREATE INDEX IF NOT EXISTS idx_member_payment_methods_gym_id ON member_payment_methods(gym_id);

-- Create gym_invoices table for platform billing (GymMate invoices to Gyms)
CREATE TABLE IF NOT EXISTS gym_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
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
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_gym_invoices_gym_id ON gym_invoices(gym_id);
CREATE INDEX IF NOT EXISTS idx_gym_invoices_stripe_invoice_id ON gym_invoices(stripe_invoice_id);

-- Create member_invoices table for membership billing (Gym invoices to Members)
CREATE TABLE IF NOT EXISTS member_invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id UUID NOT NULL,
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    membership_id UUID REFERENCES member_memberships(id) ON DELETE SET NULL,
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
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_member_invoices_member_id ON member_invoices(member_id);
CREATE INDEX IF NOT EXISTS idx_member_invoices_gym_id ON member_invoices(gym_id);
CREATE INDEX IF NOT EXISTS idx_member_invoices_membership_id ON member_invoices(membership_id);

-- Create stripe_webhook_events table for idempotency
CREATE TABLE IF NOT EXISTS stripe_webhook_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stripe_event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed BOOLEAN DEFAULT false,
    payload JSONB,
    error_message TEXT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stripe_webhook_events_stripe_event_id ON stripe_webhook_events(stripe_event_id);
CREATE INDEX IF NOT EXISTS idx_stripe_webhook_events_event_type ON stripe_webhook_events(event_type);
