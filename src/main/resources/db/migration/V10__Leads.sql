-- ============================================================================
-- V10: Lead Management Module
-- Gym-scoped sales leads: each gym in an organisation keeps its own pipeline
-- ============================================================================

CREATE TABLE IF NOT EXISTS leads (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    source VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    notes TEXT,
    assigned_to UUID,
    follow_up_date DATE,
    converted_at TIMESTAMP,
    converted_member_id UUID,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_leads_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_leads_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_leads_converted_member FOREIGN KEY (converted_member_id)
        REFERENCES members(id) ON DELETE SET NULL,
    CONSTRAINT chk_leads_status CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'TRIAL', 'CONVERTED', 'LOST'))
);

CREATE INDEX IF NOT EXISTS idx_leads_organisation ON leads(organisation_id);
CREATE INDEX IF NOT EXISTS idx_leads_gym ON leads(gym_id);
CREATE INDEX IF NOT EXISTS idx_leads_gym_status ON leads(gym_id, status) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_leads_follow_up ON leads(gym_id, follow_up_date) WHERE is_active = TRUE;

-- One active lead per email per gym (case-insensitive), matching the service-level duplicate check
CREATE UNIQUE INDEX IF NOT EXISTS uq_leads_gym_email
    ON leads(gym_id, LOWER(email))
    WHERE email IS NOT NULL AND is_active = TRUE;
