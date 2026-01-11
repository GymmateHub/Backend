-- Drop pending_registrations table as registration flow has been simplified
-- Users are now created directly with INACTIVE status and verified via OTP

DROP TABLE IF EXISTS pending_registrations;

