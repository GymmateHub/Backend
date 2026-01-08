-- V1_13: Add missing BaseAuditEntity and TenantEntity columns to equipment table
-- The equipment table is missing columns inherited from the entity hierarchy:
-- Equipment -> GymScopedEntity -> TenantEntity -> BaseAuditEntity -> BaseEntity

-- Add is_active column (from BaseAuditEntity)
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- Add audit columns (from BaseAuditEntity)
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- Add organisation_id column (from TenantEntity)
ALTER TABLE equipment ADD COLUMN IF NOT EXISTS organisation_id UUID;

-- Create index on organisation_id for tenant filtering performance
CREATE INDEX IF NOT EXISTS idx_equipment_organisation_id ON equipment(organisation_id);

-- Create index on gym_id for gym-scoped filtering (if not exists)
CREATE INDEX IF NOT EXISTS idx_equipment_gym_id ON equipment(gym_id);

-- Create index on is_active for filtering active equipment
CREATE INDEX IF NOT EXISTS idx_equipment_is_active ON equipment(is_active);

