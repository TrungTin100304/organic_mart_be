-- V17: Internal delivery infrastructure
-- Creates residential_building and delivery_slot tables.
-- Updates user_address with building FK and unit fields.
-- Updates payment_request and orders with internal delivery fields.
-- No data loss: existing records keep NULL for new nullable columns.

-- ─── Residential Building ─────────────────────────────────────────────────────────
CREATE TABLE residential_building (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_building_code ON residential_building (code);
CREATE INDEX idx_building_active ON residential_building (is_active);

-- ─── Delivery Slot ────────────────────────────────────────────────────────────────
CREATE TABLE delivery_slot (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    cutoff_minutes INT NOT NULL DEFAULT 0,
    maximum_orders INT NOT NULL DEFAULT 999,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_slot_active ON delivery_slot (is_active);
CREATE INDEX idx_slot_display ON delivery_slot (display_order);

-- Default delivery slots
INSERT INTO delivery_slot (name, start_time, end_time, cutoff_minutes, maximum_orders, display_order) VALUES
    ('08:00 - 10:00', '08:00:00', '10:00:00', 30, 10, 1),
    ('10:00 - 12:00', '10:00:00', '12:00:00', 30, 10, 2),
    ('14:00 - 16:00', '14:00:00', '16:00:00', 30, 10, 3),
    ('16:00 - 18:00', '16:00:00', '18:00:00', 30, 10, 4),
    ('18:00 - 20:00', '18:00:00', '20:00:00', 30, 10, 5);

-- ─── Update user_address ─────────────────────────────────────────────────────────
ALTER TABLE user_address
    ADD COLUMN building_id BIGINT REFERENCES residential_building(id) ON DELETE SET NULL,
    ADD COLUMN floor VARCHAR(20),
    ADD COLUMN apartment_number VARCHAR(20),
    ADD COLUMN delivery_note TEXT;

-- ─── Update payment_request ──────────────────────────────────────────────────────
ALTER TABLE payment_request
    ADD COLUMN delivery_method VARCHAR(20),
    ADD COLUMN delivery_date DATE,
    ADD COLUMN delivery_slot_id BIGINT REFERENCES delivery_slot(id) ON DELETE SET NULL,
    ADD COLUMN delivery_slot_snapshot VARCHAR(100),
    ADD COLUMN building_code_snapshot VARCHAR(20),
    ADD COLUMN building_name_snapshot VARCHAR(200),
    ADD COLUMN floor_snapshot VARCHAR(20),
    ADD COLUMN apartment_number_snapshot VARCHAR(20),
    ADD COLUMN recipient_name_snapshot VARCHAR(100),
    ADD COLUMN recipient_phone_snapshot VARCHAR(20),
    ADD COLUMN delivery_note_snapshot TEXT;

-- ─── Update orders ────────────────────────────────────────────────────────────────
ALTER TABLE orders
    ADD COLUMN delivery_method VARCHAR(20),
    ADD COLUMN delivery_date DATE,
    ADD COLUMN delivery_slot_id BIGINT REFERENCES delivery_slot(id) ON DELETE SET NULL,
    ADD COLUMN delivery_slot_snapshot VARCHAR(100),
    ADD COLUMN building_code_snapshot VARCHAR(20),
    ADD COLUMN building_name_snapshot VARCHAR(200),
    ADD COLUMN floor_snapshot VARCHAR(20),
    ADD COLUMN apartment_number_snapshot VARCHAR(20),
    ADD COLUMN recipient_name_snapshot VARCHAR(100),
    ADD COLUMN recipient_phone_snapshot VARCHAR(20),
    ADD COLUMN delivery_note_snapshot TEXT;
