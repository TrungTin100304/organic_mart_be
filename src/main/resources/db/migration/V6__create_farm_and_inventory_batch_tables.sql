-- V6__create_farm_and_inventory_batch_tables.sql
-- Batch-based inventory management and product traceability.

CREATE TABLE farm (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    certification VARCHAR(100),
    location      TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE inventory_batch (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    farm_id             BIGINT NOT NULL REFERENCES farm(id) ON DELETE RESTRICT,
    batch_code          VARCHAR(50) UNIQUE NOT NULL,
    quantity_initial    DECIMAL(12,2) NOT NULL CHECK (quantity_initial > 0),
    quantity_remaining  DECIMAL(12,2) NOT NULL CHECK (quantity_remaining >= 0),
    import_date         DATE NOT NULL,
    expiry_date         DATE NOT NULL,
    cost_price          DECIMAL(12,2),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (expiry_date > import_date),
    CHECK (quantity_remaining <= quantity_initial)
);

CREATE INDEX idx_batch_product ON inventory_batch(product_id);
CREATE INDEX idx_batch_farm ON inventory_batch(farm_id);
CREATE INDEX idx_batch_expiry_active ON inventory_batch(product_id, expiry_date)
    WHERE quantity_remaining > 0;
