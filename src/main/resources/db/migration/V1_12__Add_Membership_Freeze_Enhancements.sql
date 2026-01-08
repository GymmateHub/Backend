-- =====================================================
-- V1.12: Add Membership Freeze Enhancements
-- =====================================================
-- This migration adds comprehensive freeze/hold functionality
-- for member memberships with policy controls and tracking.

-- 1. Add freeze tracking columns to member_memberships table
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS frozen_from DATE;
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS total_days_frozen INTEGER DEFAULT 0;
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS freeze_count INTEGER DEFAULT 0;

-- 2. Create freeze_policies table
CREATE TABLE IF NOT EXISTS freeze_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,

    policy_name VARCHAR(255) NOT NULL,
    max_freeze_days_per_year INTEGER DEFAULT 90,
    max_consecutive_freeze_days INTEGER DEFAULT 60,
    min_membership_days_before_freeze INTEGER DEFAULT 30,
    cooling_off_period_days INTEGER DEFAULT 30,
    freeze_fee_amount DECIMAL(10, 2) DEFAULT 0.00,
    freeze_fee_frequency VARCHAR(20) DEFAULT 'NONE',
    allow_partial_month_freeze BOOLEAN DEFAULT TRUE,
    is_default_policy BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_freeze_policy_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_freeze_policy_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE
);

-- 3. Create indexes for freeze_policies
CREATE INDEX IF NOT EXISTS idx_freeze_policies_gym ON freeze_policies(gym_id);
CREATE INDEX IF NOT EXISTS idx_freeze_policies_organisation ON freeze_policies(organisation_id);
CREATE INDEX IF NOT EXISTS idx_freeze_policies_active ON freeze_policies(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_freeze_policies_default ON freeze_policies(is_default_policy) WHERE is_default_policy = TRUE;

-- 4. Create index for frozen memberships (for auto-unfreeze job)
CREATE INDEX IF NOT EXISTS idx_member_memberships_frozen ON member_memberships(frozen_until) WHERE is_frozen = TRUE;

-- 5. Add comments for documentation
COMMENT ON COLUMN member_memberships.frozen_from IS 'Date when the membership freeze started';
COMMENT ON COLUMN member_memberships.total_days_frozen IS 'Cumulative total days this membership has been frozen (for policy limits)';
COMMENT ON COLUMN member_memberships.freeze_count IS 'Number of times this membership has been frozen (for policy tracking)';

COMMENT ON TABLE freeze_policies IS 'Policies defining business rules for membership freezing per gym';
COMMENT ON COLUMN freeze_policies.max_freeze_days_per_year IS 'Maximum total days a membership can be frozen per year (default: 90)';
COMMENT ON COLUMN freeze_policies.max_consecutive_freeze_days IS 'Maximum consecutive days for a single freeze period (default: 60)';
COMMENT ON COLUMN freeze_policies.min_membership_days_before_freeze IS 'Minimum days a membership must be active before first freeze (default: 30)';
COMMENT ON COLUMN freeze_policies.cooling_off_period_days IS 'Required days between freeze periods (default: 30)';
COMMENT ON COLUMN freeze_policies.freeze_fee_amount IS 'Fee charged for freezing (default: 0.00)';
COMMENT ON COLUMN freeze_policies.freeze_fee_frequency IS 'Fee frequency: NONE, ONE_TIME, or MONTHLY';
COMMENT ON COLUMN freeze_policies.allow_partial_month_freeze IS 'Whether partial month freezes are allowed';
COMMENT ON COLUMN freeze_policies.is_default_policy IS 'Whether this is the default policy for the organisation';
