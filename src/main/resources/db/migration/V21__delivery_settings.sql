CREATE TABLE IF NOT EXISTS delivery_settings (
    id BIGSERIAL PRIMARY KEY,
    delivery_type VARCHAR(20) NOT NULL UNIQUE,
    fee DECIMAL(12,2) NOT NULL DEFAULT 0,
    estimated_minutes INTEGER,
    display_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO delivery_settings (delivery_type, fee, estimated_minutes, display_order, enabled)
VALUES
    ('STANDARD', 0, 60, 1, TRUE),
    ('EXPRESS', 20000, 30, 2, TRUE),
    ('SCHEDULED', 0, NULL, 3, TRUE)
ON CONFLICT (delivery_type) DO NOTHING;

