-- ============================================================================
-- V2: Newsletter & Message Templating Schema
-- ============================================================================
-- Migration for newsletter templates, campaigns, and recipient tracking.
-- Generated: 2026-01-27
-- ============================================================================

-- ============================================================================
-- SECTION 1: NEWSLETTER TEMPLATES
-- ============================================================================

CREATE TABLE IF NOT EXISTS newsletter_templates (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    template_type VARCHAR(20) DEFAULT 'EMAIL',
    placeholders JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_newsletter_templates_org ON newsletter_templates(organisation_id);
CREATE INDEX IF NOT EXISTS idx_newsletter_templates_gym ON newsletter_templates(gym_id);
CREATE INDEX IF NOT EXISTS idx_newsletter_templates_active ON newsletter_templates(gym_id, active);

-- ============================================================================
-- SECTION 2: NEWSLETTER CAMPAIGNS
-- ============================================================================

CREATE TABLE IF NOT EXISTS newsletter_campaigns (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    template_id UUID,
    name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    audience_type VARCHAR(30) NOT NULL,
    audience_filter JSONB,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    total_recipients INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    sent_by_user_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_org ON newsletter_campaigns(organisation_id);
CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_gym ON newsletter_campaigns(gym_id);
CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_status ON newsletter_campaigns(gym_id, status);
CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_template ON newsletter_campaigns(template_id);

-- ============================================================================
-- SECTION 3: CAMPAIGN RECIPIENTS
-- ============================================================================

CREATE TABLE IF NOT EXISTS campaign_recipients (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    campaign_id UUID NOT NULL,
    member_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_campaign_recipients_campaign ON campaign_recipients(campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_recipients_member ON campaign_recipients(member_id);
CREATE INDEX IF NOT EXISTS idx_campaign_recipients_status ON campaign_recipients(campaign_id, status);

-- ============================================================================
-- SECTION 4: FOREIGN KEY CONSTRAINTS
-- ============================================================================

DO $$
BEGIN
    -- Newsletter templates
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_newsletter_templates_org') THEN
        ALTER TABLE newsletter_templates ADD CONSTRAINT fk_newsletter_templates_org 
            FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_newsletter_templates_gym') THEN
        ALTER TABLE newsletter_templates ADD CONSTRAINT fk_newsletter_templates_gym 
            FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    -- Newsletter campaigns
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_newsletter_campaigns_org') THEN
        ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_org 
            FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_newsletter_campaigns_gym') THEN
        ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_gym 
            FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_newsletter_campaigns_template') THEN
        ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_template 
            FOREIGN KEY (template_id) REFERENCES newsletter_templates(id) ON DELETE SET NULL;
    END IF;

    -- Campaign recipients
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_campaign_recipients_campaign') THEN
        ALTER TABLE campaign_recipients ADD CONSTRAINT fk_campaign_recipients_campaign 
            FOREIGN KEY (campaign_id) REFERENCES newsletter_campaigns(id) ON DELETE CASCADE;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_campaign_recipients_member') THEN
        ALTER TABLE campaign_recipients ADD CONSTRAINT fk_campaign_recipients_member 
            FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;
    END IF;

    RAISE NOTICE 'Newsletter foreign key constraints added successfully';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Error adding newsletter foreign key constraints: %', SQLERRM;
END;
$$;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
-- Tables created: 3
--   - newsletter_templates: Reusable message templates
--   - newsletter_campaigns: Bulk email campaigns
--   - campaign_recipients: Individual recipient tracking
-- ============================================================================
