-- Create pending_registrations table for OTP-based registration flow
CREATE TABLE IF NOT EXISTS pending_registrations (
    registration_id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_otp_sent_at TIMESTAMP,
    otp_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_pending_reg_email ON pending_registrations(email);
CREATE INDEX IF NOT EXISTS idx_pending_reg_expires_at ON pending_registrations(expires_at);

-- Add comment
COMMENT ON TABLE pending_registrations IS 'Stores temporary registration data for OTP verification flow';

