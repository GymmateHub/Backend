-- V1_1__Add_Missing_Columns_To_Equipment.sql
-- Migration script for Inventory domain tables
-- Creates: suppliers, equipment, inventory_items, maintenance_records, maintenance_schedules, stock_movements

-- =============================================
-- SUPPLIERS TABLE (Organisation-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS suppliers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,

    -- Basic info
    name VARCHAR(200) NOT NULL,
    code VARCHAR(100),
    description TEXT,

    -- Contact information
    contact_person VARCHAR(200),
    email VARCHAR(100),
    phone VARCHAR(20),
    mobile_phone VARCHAR(20),
    website VARCHAR(500),

    -- Address
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(50),
    country VARCHAR(50),
    postal_code VARCHAR(20),

    -- Business details
    tax_id VARCHAR(100),
    payment_terms VARCHAR(100),
    currency VARCHAR(3) DEFAULT 'USD',
    credit_limit NUMERIC(10,2),

    -- Category and rating
    supplier_category VARCHAR(100),
    rating INTEGER DEFAULT 0,
    is_preferred BOOLEAN DEFAULT FALSE,
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_suppliers_organisation_id ON suppliers(organisation_id);
CREATE INDEX IF NOT EXISTS idx_suppliers_name ON suppliers(name);
CREATE INDEX IF NOT EXISTS idx_suppliers_code ON suppliers(code);

-- =============================================
-- EQUIPMENT TABLE (Gym-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS equipment (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,

    -- Basic info
    name VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    description TEXT,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),

    -- Status and tracking
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    purchase_date DATE,
    purchase_price NUMERIC(10,2),
    current_value NUMERIC(10,2),

    -- Warranty information
    warranty_expiry_date DATE,
    warranty_provider VARCHAR(200),

    -- Location and assignment
    area_id UUID,
    location_notes TEXT,

    -- Maintenance
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    maintenance_interval_days INTEGER DEFAULT 90,
    total_maintenance_cost NUMERIC(10,2) DEFAULT 0,

    -- Usage tracking
    usage_hours INTEGER DEFAULT 0,
    max_capacity INTEGER,

    -- Supplier reference
    supplier_id UUID,

    -- Additional info
    image_url VARCHAR(500),
    notes TEXT,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,

    -- Constraints
    CONSTRAINT chk_equipment_category CHECK (category IN ('CARDIO', 'STRENGTH', 'FUNCTIONAL', 'BOXING', 'YOGA', 'SWIMMING', 'SPORTS', 'ACCESSIBILITY', 'RECOVERY', 'OTHER')),
    CONSTRAINT chk_equipment_status CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'RETIRED', 'ORDERED', 'DAMAGED'))
);

CREATE INDEX IF NOT EXISTS idx_equipment_organisation_id ON equipment(organisation_id);
CREATE INDEX IF NOT EXISTS idx_equipment_gym_id ON equipment(gym_id);
CREATE INDEX IF NOT EXISTS idx_equipment_category ON equipment(category);
CREATE INDEX IF NOT EXISTS idx_equipment_status ON equipment(status);
CREATE INDEX IF NOT EXISTS idx_equipment_supplier_id ON equipment(supplier_id);

-- =============================================
-- INVENTORY_ITEMS TABLE (Gym-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,

    -- Basic info
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) UNIQUE,
    category VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    description TEXT,

    -- Stock levels
    current_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock INTEGER DEFAULT 0,
    maximum_stock INTEGER,
    reorder_point INTEGER DEFAULT 0,
    reorder_quantity INTEGER,

    -- Pricing
    unit_cost NUMERIC(10,2),
    unit_price NUMERIC(10,2),
    unit VARCHAR(20),

    -- Supplier information
    supplier_id UUID,
    supplier_product_code VARCHAR(100),

    -- Tracking
    barcode VARCHAR(100),
    location VARCHAR(200),
    expiry_tracking BOOLEAN DEFAULT FALSE,
    batch_tracking BOOLEAN DEFAULT FALSE,

    -- Additional info
    image_url VARCHAR(500),
    notes TEXT,
    low_stock_alert_sent BOOLEAN DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,

    -- Constraints
    CONSTRAINT chk_inventory_category CHECK (category IN ('SUPPLEMENTS', 'APPAREL', 'ACCESSORIES', 'SUPPLIES', 'MERCHANDISE', 'EQUIPMENT_PARTS', 'OTHER'))
);

CREATE INDEX IF NOT EXISTS idx_inventory_items_organisation_id ON inventory_items(organisation_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_gym_id ON inventory_items(gym_id);
CREATE INDEX IF NOT EXISTS idx_inventory_items_sku ON inventory_items(sku);
CREATE INDEX IF NOT EXISTS idx_inventory_items_category ON inventory_items(category);
CREATE INDEX IF NOT EXISTS idx_inventory_items_supplier_id ON inventory_items(supplier_id);

-- =============================================
-- MAINTENANCE_RECORDS TABLE (Gym-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_records (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,

    -- Equipment reference
    equipment_id UUID NOT NULL,

    -- Maintenance details
    maintenance_date DATE NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    description TEXT,
    performed_by VARCHAR(200),
    technician_company VARCHAR(200),

    -- Cost
    cost NUMERIC(10,2),
    parts_replaced TEXT,

    -- Scheduling
    next_maintenance_due DATE,

    -- Documentation
    notes TEXT,
    invoice_number VARCHAR(100),
    invoice_url VARCHAR(500),

    -- Completion status
    is_completed BOOLEAN DEFAULT TRUE,
    completion_notes TEXT,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_maintenance_records_organisation_id ON maintenance_records(organisation_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_gym_id ON maintenance_records(gym_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_equipment_id ON maintenance_records(equipment_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_maintenance_date ON maintenance_records(maintenance_date);

-- =============================================
-- MAINTENANCE_SCHEDULES TABLE (Gym-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS maintenance_schedules (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,

    -- Equipment reference
    equipment_id UUID NOT NULL,

    -- Schedule details
    schedule_name VARCHAR(200) NOT NULL,
    description TEXT,
    scheduled_date DATE NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,

    -- Assignment
    assigned_to VARCHAR(200),
    estimated_duration_hours INTEGER,

    -- Recurrence
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_interval_days INTEGER,

    -- Completion
    is_completed BOOLEAN DEFAULT FALSE,
    completed_date DATE,
    maintenance_record_id UUID,

    -- Additional info
    notes TEXT,

    -- Reminders
    reminder_sent BOOLEAN DEFAULT FALSE,
    reminder_date DATE,

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_organisation_id ON maintenance_schedules(organisation_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_gym_id ON maintenance_schedules(gym_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_equipment_id ON maintenance_schedules(equipment_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_scheduled_date ON maintenance_schedules(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_maintenance_schedules_is_completed ON maintenance_schedules(is_completed);

-- =============================================
-- STOCK_MOVEMENTS TABLE (Gym-scoped)
-- =============================================
CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID,
    gym_id UUID,

    -- Item reference
    inventory_item_id UUID NOT NULL,

    -- Movement details
    movement_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost NUMERIC(10,2),
    total_cost NUMERIC(10,2),

    -- Stock tracking
    stock_before INTEGER NOT NULL,
    stock_after INTEGER NOT NULL,
    movement_date TIMESTAMP(6) NOT NULL DEFAULT NOW(),

    -- References
    reference_number VARCHAR(100),
    supplier_id UUID,
    customer_id UUID,
    from_gym_id UUID,
    to_gym_id UUID,
    batch_number VARCHAR(100),

    -- Additional info
    notes TEXT,
    performed_by VARCHAR(255),

    -- Audit fields
    created_at TIMESTAMP(6) NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_at TIMESTAMP(6),
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,

    -- Constraints
    CONSTRAINT chk_movement_type CHECK (movement_type IN ('PURCHASE', 'SALE', 'ADJUSTMENT', 'DAMAGE', 'RETURN', 'TRANSFER_IN', 'TRANSFER_OUT', 'INITIAL_STOCK'))
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_organisation_id ON stock_movements(organisation_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_gym_id ON stock_movements(gym_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_inventory_item_id ON stock_movements(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_movement_type ON stock_movements(movement_type);
CREATE INDEX IF NOT EXISTS idx_stock_movements_movement_date ON stock_movements(movement_date);

-- =============================================
-- FOREIGN KEY CONSTRAINTS (Optional - for referential integrity)
-- =============================================
-- Note: These are commented out to allow for flexibility during development.
-- Uncomment in production if you want strict referential integrity.

-- ALTER TABLE equipment ADD CONSTRAINT fk_equipment_supplier
--     FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

-- ALTER TABLE inventory_items ADD CONSTRAINT fk_inventory_items_supplier
--     FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

-- ALTER TABLE maintenance_records ADD CONSTRAINT fk_maintenance_records_equipment
--     FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE;

-- ALTER TABLE maintenance_schedules ADD CONSTRAINT fk_maintenance_schedules_equipment
--     FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE;

-- ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_inventory_item
--     FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE;

-- ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_movements_supplier
--     FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

