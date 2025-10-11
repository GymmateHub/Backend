-- Create composite indexes for improved query performance
-- These indexes are not duplicates of those in V4
CREATE INDEX IF NOT EXISTS idx_users_gym_email ON users(gym_id, email);
CREATE INDEX IF NOT EXISTS idx_users_email_role ON users(email, role);

