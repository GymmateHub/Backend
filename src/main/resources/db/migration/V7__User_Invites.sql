-- Create uuid-ossp extension if not exists (for uuidv7)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE user_invites (
  id              UUID PRIMARY KEY DEFAULT uuidv7(),
  gym_id          UUID NOT NULL REFERENCES gyms(id),
  organisation_id UUID NOT NULL REFERENCES organisations(id),
  invited_by      UUID NOT NULL REFERENCES users(id),

  email           VARCHAR(255) NOT NULL,
  role            VARCHAR(50)  NOT NULL,
  first_name      VARCHAR(100),
  last_name       VARCHAR(100),

  token           VARCHAR(255) NOT NULL UNIQUE,
  token_hash      VARCHAR(255) NOT NULL,
  status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING',

  expires_at      TIMESTAMPTZ  NOT NULL DEFAULT (NOW() + INTERVAL '72 hours'),
  accepted_at     TIMESTAMPTZ,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invites_token_hash ON user_invites(token_hash);
CREATE INDEX idx_invites_email_gym  ON user_invites(email, gym_id);
CREATE INDEX idx_invites_status     ON user_invites(status);
