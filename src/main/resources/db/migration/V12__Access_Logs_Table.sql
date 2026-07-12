-- ============================================================
-- V12: access_logs table
--
-- The AccessLog entity (com.gymmate.access.domain.AccessLog) was introduced with
-- the access-control feature but its table was omitted from V10, so JPA
-- ddl-auto=validate fails on startup with "missing table [access_logs]".
-- This migration creates the table to match the entity + its GymScopedEntity
-- audit base class (mirrors the conventions used for access_events in V10).
-- ============================================================
CREATE TABLE IF NOT EXISTS access_logs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,
    member_id UUID NOT NULL,
    access_time TIMESTAMP NOT NULL,
    direction VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    access_method VARCHAR(50) NOT NULL,
    denial_reason VARCHAR(255),
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_access_logs_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_access_logs_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT chk_access_logs_direction CHECK (direction IN ('ENTRY', 'EXIT')),
    CONSTRAINT chk_access_logs_status CHECK (status IN (
        'GRANTED', 'DENIED_PASSBACK', 'DENIED_MEMBERSHIP', 'DENIED_LOCKOUT', 'ALERT_TAILGATING'
    ))
);

CREATE INDEX IF NOT EXISTS idx_access_member ON access_logs(member_id, access_time DESC);
CREATE INDEX IF NOT EXISTS idx_access_logs_gym ON access_logs(gym_id);
