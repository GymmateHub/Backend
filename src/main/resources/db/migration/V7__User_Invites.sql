-- ============================================================================
-- V4: User Invites Table
-- ============================================================================
-- Supports invite-only registration for ADMIN, TRAINER, STAFF roles.
-- Invited users receive an email link to set their password and activate account.
-- ============================================================================

CREATE TABLE IF NOT EXISTS user_invites (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    gym_id          UUID NOT NULL,
    organisation_id UUID NOT NULL,
    invited_by      UUID NOT NULL,

    -- Invitee details
    email           VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,   -- ADMIN, TRAINER, STAFF only
    first_name      VARCHAR(100),            -- Optional pre-fill by inviter
    last_name       VARCHAR(100),            -- Optional pre-fill by inviter

    -- Token (store hash, not plaintext for security)
    token_hash      VARCHAR(255) NOT NULL UNIQUE,

    -- Status lifecycle: PENDING -> ACCEPTED | EXPIRED | REVOKED
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',

    -- Timestamps
    expires_at      TIMESTAMP    NOT NULL DEFAULT (NOW() + INTERVAL '72 hours'),
    accepted_at     TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_invites_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_invites_organisation FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_invites_invited_by FOREIGN KEY (invited_by) REFERENCES users(id) ON DELETE CASCADE,

    -- Ensure role is one of the invite-only roles
    CONSTRAINT chk_invite_role CHECK (role IN ('ADMIN', 'TRAINER', 'STAFF')),

    -- Ensure status is valid
    CONSTRAINT chk_invite_status CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'REVOKED'))
);

-- Index for token lookup (primary query path for invite validation)
CREATE INDEX IF NOT EXISTS idx_invites_token_hash ON user_invites(token_hash);

-- Index for checking existing invites for same email+gym
CREATE INDEX IF NOT EXISTS idx_invites_email_gym ON user_invites(email, gym_id);

-- Index for status-based queries (cleanup job, listing pending invites)
CREATE INDEX IF NOT EXISTS idx_invites_status ON user_invites(status);

-- Index for expiration cleanup job
CREATE INDEX IF NOT EXISTS idx_invites_expires_at ON user_invites(expires_at) WHERE status = 'PENDING';

-- Index for listing invites by gym (admin dashboard)
CREATE INDEX IF NOT EXISTS idx_invites_gym_id ON user_invites(gym_id);

-- Composite index for the most common query pattern
CREATE INDEX IF NOT EXISTS idx_invites_pending_expires ON user_invites(status, expires_at)
    WHERE status = 'PENDING';

COMMENT ON TABLE user_invites IS 'Tracks invitations for ADMIN, TRAINER, STAFF roles. Self-registration is only allowed for OWNER and MEMBER roles.';
COMMENT ON COLUMN user_invites.token_hash IS 'SHA-256 hash of the invite token. Raw token is sent via email and never stored.';
COMMENT ON COLUMN user_invites.status IS 'PENDING=awaiting acceptance, ACCEPTED=user created account, EXPIRED=past expires_at, REVOKED=cancelled by admin';

