-- ================================================
-- V10: Access Control & Anti-Tailgating Module
-- ================================================

-- Access points (controlled entries: doors, turnstiles, gates)
CREATE TABLE IF NOT EXISTS access_points (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL DEFAULT 'MAIN_DOOR',
    mode VARCHAR(20) NOT NULL DEFAULT 'SOFTWARE',
    area_id UUID,
    device_id VARCHAR(100),
    online BOOLEAN DEFAULT TRUE,
    reentry_lockout_seconds INTEGER DEFAULT 300,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_access_points_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_points_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT chk_access_points_type CHECK (type IN ('MAIN_DOOR', 'TURNSTILE', 'STUDIO_DOOR', 'GATE')),
    CONSTRAINT chk_access_points_mode CHECK (mode IN ('SOFTWARE', 'TURNSTILE', 'CV'))
);
CREATE INDEX IF NOT EXISTS idx_access_points_gym ON access_points(gym_id);
CREATE INDEX IF NOT EXISTS idx_access_points_organisation ON access_points(organisation_id);

-- Access credentials (per-member QR/PIN/NFC; only the token hash is stored)
CREATE TABLE IF NOT EXISTS access_credentials (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    member_id UUID NOT NULL,
    type VARCHAR(10) NOT NULL DEFAULT 'QR',
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_access_credentials_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_credentials_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_credentials_member FOREIGN KEY (member_id)
        REFERENCES members(id) ON DELETE CASCADE,
    CONSTRAINT chk_access_credentials_type CHECK (type IN ('QR', 'PIN', 'NFC'))
);
CREATE INDEX IF NOT EXISTS idx_access_credentials_gym ON access_credentials(gym_id);
CREATE INDEX IF NOT EXISTS idx_access_credentials_member ON access_credentials(member_id);
CREATE INDEX IF NOT EXISTS idx_access_credentials_token_hash ON access_credentials(token_hash);

-- Door benefits (which membership plan opens which access point)
CREATE TABLE IF NOT EXISTS door_benefits (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    access_point_id UUID NOT NULL,
    membership_plan_id UUID NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_door_benefits_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_door_benefits_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_door_benefits_access_point FOREIGN KEY (access_point_id)
        REFERENCES access_points(id) ON DELETE CASCADE,
    CONSTRAINT uq_door_benefits UNIQUE (access_point_id, membership_plan_id)
);
CREATE INDEX IF NOT EXISTS idx_door_benefits_access_point ON door_benefits(access_point_id);

-- Access schedules (allowed entry windows per membership plan)
CREATE TABLE IF NOT EXISTS access_schedules (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    membership_plan_id UUID NOT NULL,
    day_of_week VARCHAR(10),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_access_schedules_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_schedules_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_access_schedules_plan ON access_schedules(membership_plan_id);

-- Access events (append-only audit trail / Visitors log)
CREATE TABLE IF NOT EXISTS access_events (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    member_id UUID,
    access_point_id UUID NOT NULL,
    credential_id UUID,
    direction VARCHAR(10) NOT NULL DEFAULT 'IN',
    decision VARCHAR(10) NOT NULL,
    deny_reason VARCHAR(30),
    tailgating_suspected BOOLEAN DEFAULT FALSE,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_scan_count INTEGER,
    device_pass_count INTEGER,
    captured_image_url VARCHAR(500),
    note TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_access_events_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_events_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_events_access_point FOREIGN KEY (access_point_id)
        REFERENCES access_points(id) ON DELETE CASCADE,
    CONSTRAINT chk_access_events_direction CHECK (direction IN ('IN', 'OUT')),
    CONSTRAINT chk_access_events_decision CHECK (decision IN ('GRANTED', 'DENIED'))
);
CREATE INDEX IF NOT EXISTS idx_access_events_gym ON access_events(gym_id);
CREATE INDEX IF NOT EXISTS idx_access_events_member ON access_events(member_id);
CREATE INDEX IF NOT EXISTS idx_access_events_occurred_at ON access_events(occurred_at);
CREATE INDEX IF NOT EXISTS idx_access_events_tailgating ON access_events(tailgating_suspected);
CREATE INDEX IF NOT EXISTS idx_access_events_credential ON access_events(credential_id);
