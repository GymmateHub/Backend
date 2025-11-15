-- Create token blacklist table for JWT token revocation
CREATE TABLE IF NOT EXISTS token_blacklist (
    id UUID PRIMARY KEY,
    token VARCHAR(1000) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason VARCHAR(255)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_token_blacklist_token ON token_blacklist(token);
CREATE INDEX IF NOT EXISTS idx_token_blacklist_expires_at ON token_blacklist(expires_at);
CREATE INDEX IF NOT EXISTS idx_token_blacklist_user_id ON token_blacklist(user_id);

-- Add comment to table
COMMENT ON TABLE token_blacklist IS 'Stores blacklisted JWT tokens for logout and revocation';
COMMENT ON COLUMN token_blacklist.token IS 'The JWT token string';
COMMENT ON COLUMN token_blacklist.user_id IS 'The user ID associated with this token';
COMMENT ON COLUMN token_blacklist.blacklisted_at IS 'When the token was blacklisted';
COMMENT ON COLUMN token_blacklist.expires_at IS 'When the token expires (can be deleted after this)';
COMMENT ON COLUMN token_blacklist.reason IS 'Reason for blacklisting (e.g., user logout, admin revocation)';

