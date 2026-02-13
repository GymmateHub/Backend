-- ================================================
-- V4: POS (Point of Sale) Module Tables
-- ================================================

-- Create uuid-ossp extension if not exists (for uuidv7)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- POS Sales table
CREATE TABLE IF NOT EXISTS pos_sales (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    sale_number VARCHAR(50) NOT NULL UNIQUE,
    member_id UUID,
    customer_name VARCHAR(200),
    staff_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_type VARCHAR(30) NOT NULL DEFAULT 'CASH',
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    discount_percentage DECIMAL(5, 2),
    discount_code VARCHAR(50),
    tax_amount DECIMAL(12, 2) DEFAULT 0.00,
    tax_rate DECIMAL(5, 2),
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    amount_paid DECIMAL(12, 2) DEFAULT 0.00,
    change_given DECIMAL(12, 2) DEFAULT 0.00,
    refunded_amount DECIMAL(12, 2) DEFAULT 0.00,
    stripe_payment_intent_id VARCHAR(100),
    external_reference VARCHAR(100),
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    notes TEXT,
    receipt_printed BOOLEAN DEFAULT FALSE,
    receipt_emailed BOOLEAN DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_pos_sales_organisation FOREIGN KEY (organisation_id) 
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_sales_gym FOREIGN KEY (gym_id) 
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_sales_member FOREIGN KEY (member_id) 
        REFERENCES members(id) ON DELETE SET NULL,
    CONSTRAINT fk_pos_sales_staff FOREIGN KEY (staff_id) 
        REFERENCES staff(id) ON DELETE SET NULL,
    CONSTRAINT chk_pos_sales_status CHECK (status IN ('PENDING', 'COMPLETED', 'REFUNDED', 'PARTIALLY_REFUNDED', 'CANCELLED', 'VOID')),
    CONSTRAINT chk_pos_sales_payment_type CHECK (payment_type IN ('CASH', 'CARD', 'STRIPE', 'MEMBER_ACCOUNT', 'SPLIT', 'GIFT_VOUCHER', 'CREDIT_NOTE'))
);

-- Create indexes for pos_sales
CREATE INDEX IF NOT EXISTS idx_pos_sales_organisation ON pos_sales(organisation_id);
CREATE INDEX IF NOT EXISTS idx_pos_sales_gym ON pos_sales(gym_id);
CREATE INDEX IF NOT EXISTS idx_pos_sales_member ON pos_sales(member_id);
CREATE INDEX IF NOT EXISTS idx_pos_sales_staff ON pos_sales(staff_id);
CREATE INDEX IF NOT EXISTS idx_pos_sales_status ON pos_sales(status);
CREATE INDEX IF NOT EXISTS idx_pos_sales_sale_date ON pos_sales(sale_date);
CREATE INDEX IF NOT EXISTS idx_pos_sales_sale_number ON pos_sales(sale_number);
CREATE INDEX IF NOT EXISTS idx_pos_sales_gym_date ON pos_sales(gym_id, sale_date);
CREATE INDEX IF NOT EXISTS idx_pos_sales_gym_status ON pos_sales(gym_id, status);

-- POS Sale Items table
CREATE TABLE IF NOT EXISTS pos_sale_items (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    inventory_item_id UUID,
    item_name VARCHAR(200) NOT NULL,
    item_sku VARCHAR(100),
    item_barcode VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    cost_price DECIMAL(12, 2),
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    discount_percentage DECIMAL(5, 2),
    line_total DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    refunded BOOLEAN DEFAULT FALSE,
    refunded_quantity INTEGER DEFAULT 0,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_pos_sale_items_sale FOREIGN KEY (sale_id) 
        REFERENCES pos_sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_sale_items_inventory FOREIGN KEY (inventory_item_id) 
        REFERENCES inventory_items(id) ON DELETE SET NULL,
    CONSTRAINT fk_pos_sale_items_organisation FOREIGN KEY (organisation_id) 
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_sale_items_gym FOREIGN KEY (gym_id) 
        REFERENCES gyms(id) ON DELETE CASCADE
);

-- Create indexes for pos_sale_items
CREATE INDEX IF NOT EXISTS idx_pos_sale_items_sale ON pos_sale_items(sale_id);
CREATE INDEX IF NOT EXISTS idx_pos_sale_items_inventory ON pos_sale_items(inventory_item_id);
CREATE INDEX IF NOT EXISTS idx_pos_sale_items_organisation ON pos_sale_items(organisation_id);
CREATE INDEX IF NOT EXISTS idx_pos_sale_items_gym ON pos_sale_items(gym_id);

-- POS Cash Drawers table
CREATE TABLE IF NOT EXISTS pos_cash_drawers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID NOT NULL,
    session_date DATE NOT NULL,
    opened_by UUID NOT NULL,
    closed_by UUID,
    opening_balance DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    closing_balance DECIMAL(12, 2),
    expected_balance DECIMAL(12, 2),
    variance DECIMAL(12, 2),
    total_cash_sales DECIMAL(12, 2) DEFAULT 0.00,
    total_card_sales DECIMAL(12, 2) DEFAULT 0.00,
    total_other_sales DECIMAL(12, 2) DEFAULT 0.00,
    total_refunds DECIMAL(12, 2) DEFAULT 0.00,
    transaction_count INTEGER DEFAULT 0,
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP,
    is_open BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT,
    closing_notes TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_pos_cash_drawers_organisation FOREIGN KEY (organisation_id) 
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_cash_drawers_gym FOREIGN KEY (gym_id) 
        REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT fk_pos_cash_drawers_opened_by FOREIGN KEY (opened_by) 
        REFERENCES staff(id) ON DELETE SET NULL,
    CONSTRAINT fk_pos_cash_drawers_closed_by FOREIGN KEY (closed_by) 
        REFERENCES staff(id) ON DELETE SET NULL
);

-- Create indexes for pos_cash_drawers
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_organisation ON pos_cash_drawers(organisation_id);
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_gym ON pos_cash_drawers(gym_id);
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_session_date ON pos_cash_drawers(session_date);
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_is_open ON pos_cash_drawers(is_open);
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_gym_open ON pos_cash_drawers(gym_id, is_open);
CREATE INDEX IF NOT EXISTS idx_pos_cash_drawers_gym_date ON pos_cash_drawers(gym_id, session_date);

-- Add comments
COMMENT ON TABLE pos_sales IS 'Point of Sale transactions';
COMMENT ON TABLE pos_sale_items IS 'Individual line items in POS sales';
COMMENT ON TABLE pos_cash_drawers IS 'Cash drawer/register sessions for POS';
