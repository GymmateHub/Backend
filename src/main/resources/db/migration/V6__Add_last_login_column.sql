-- Add last_login column if it doesn't exist
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_login TIMESTAMP;

