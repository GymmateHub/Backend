-- =====================================================
-- V1.8: Create Multi-Tenant Organisation Architecture
-- =====================================================

-- 1. Create organisations table (tenant/billing entity)
CREATE TABLE organisations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,

    -- Owner reference (will be set after user creation)
    owner_user_id UUID,

    -- Subscription & Billing (SaaS level)
    subscription_plan VARCHAR(50) DEFAULT 'starter',
    subscription_status VARCHAR(20) DEFAULT 'trial',
    subscription_started_at TIMESTAMP,
    subscription_expires_at TIMESTAMP,
    trial_ends_at TIMESTAMP,

    -- Plan limits
    max_gyms INTEGER DEFAULT 1,
    max_members INTEGER DEFAULT 200,
    max_staff INTEGER DEFAULT 10,

    -- Billing information
    billing_email VARCHAR(255),
    billing_address JSONB,
    payment_method_id VARCHAR(255),

    -- Features enabled based on plan
    features_enabled JSONB DEFAULT '[]'::jsonb,

    -- Contact information
    contact_email VARCHAR(255),
    contact_phone VARCHAR(20),

    -- Status
    is_active BOOLEAN DEFAULT true,
    onboarding_completed BOOLEAN DEFAULT false,

    -- Metadata
    settings JSONB DEFAULT '{}'::jsonb,

    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add organisation_id to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS organisation_id UUID;

-- 3. Add organisation_id to gyms table
ALTER TABLE gyms ADD COLUMN IF NOT EXISTS organisation_id UUID;

-- 4. Add gym_id to members table (members belong to a specific gym)
ALTER TABLE members ADD COLUMN IF NOT EXISTS gym_id UUID;

-- 5. Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_organisations_slug ON organisations(slug);
CREATE INDEX IF NOT EXISTS idx_organisations_owner ON organisations(owner_user_id);
CREATE INDEX IF NOT EXISTS idx_organisations_status ON organisations(subscription_status);
CREATE INDEX IF NOT EXISTS idx_users_organisation ON users(organisation_id);
CREATE INDEX IF NOT EXISTS idx_gyms_organisation ON gyms(organisation_id);
CREATE INDEX IF NOT EXISTS idx_members_gym ON members(gym_id);

-- 6. Add foreign key constraints (after data migration)
-- Note: These will be added in a separate migration after data is migrated
-- ALTER TABLE organisations ADD CONSTRAINT fk_organisations_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL;
-- ALTER TABLE users ADD CONSTRAINT fk_users_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
-- ALTER TABLE gyms ADD CONSTRAINT fk_gyms_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
-- ALTER TABLE members ADD CONSTRAINT fk_members_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;

-- 7. Add update trigger for organisations
CREATE OR REPLACE FUNCTION update_organisations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_organisations_updated_at
    BEFORE UPDATE ON organisations
    FOR EACH ROW
    EXECUTE FUNCTION update_organisations_updated_at();

COMMENT ON TABLE organisations IS 'Multi-tenant organisations (billing entities) - each can own multiple gyms';
COMMENT ON COLUMN organisations.owner_user_id IS 'Primary owner/admin user - set after user creation';
COMMENT ON COLUMN users.organisation_id IS 'Organisation this user belongs to';
COMMENT ON COLUMN gyms.organisation_id IS 'Organisation that owns this gym';
COMMENT ON COLUMN members.gym_id IS 'Gym where this member is registered';

