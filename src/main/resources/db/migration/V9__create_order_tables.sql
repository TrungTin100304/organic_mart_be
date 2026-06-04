-- V9__create_order_tables.sql
-- Order management tables.

CREATE TYPE order_status AS ENUM ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED');

CREATE TYPE promotion_type AS ENUM ('PERCENTAGE', 'FIXED_AMOUNT');

CREATE TABLE promotion (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) UNIQUE NOT NULL,
    name                VARCHAR(200) NOT NULL,
    description         TEXT,
    type                promotion_type NOT NULL,
    value               DECIMAL(12,2) NOT NULL CHECK (value > 0),
    min_order_amount    DECIMAL(12,2) DEFAULT 0,
    max_discount_amount DECIMAL(12,2),
    valid_from          DATE NOT NULL,
    valid_to            DATE NOT NULL,
    usage_limit         INT,
    usage_limit_per_user INT DEFAULT 1,
    times_used          INT NOT NULL DEFAULT 0,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (valid_to >= valid_from)
);

CREATE TABLE orders (
    id                              BIGSERIAL PRIMARY KEY,
    user_id                         BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    address_id                      BIGINT NOT NULL REFERENCES user_address(id) ON DELETE RESTRICT,
    promotion_id                    BIGINT REFERENCES promotion(id) ON DELETE SET NULL,
    order_code                      VARCHAR(20) UNIQUE NOT NULL,
    subtotal                        DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    discount_amount                 DECIMAL(12,2) NOT NULL DEFAULT 0,
    shipping_fee                    DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount                    DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    status                          order_status NOT NULL DEFAULT 'PENDING',
    note                            TEXT,
    shipping_recipient_snapshot     VARCHAR(100) NOT NULL,
    shipping_phone_snapshot         VARCHAR(20) NOT NULL,
    shipping_address_snapshot       TEXT NOT NULL,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orders_user    ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

CREATE TABLE order_detail (
    id                  BIGSERIAL PRIMARY KEY,
    order_id            BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id          BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    batch_id            BIGINT REFERENCES inventory_batch(id) ON DELETE RESTRICT,
    quantity            DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    price_at_purchase   DECIMAL(12,2) NOT NULL CHECK (price_at_purchase >= 0)
);
CREATE INDEX idx_order_detail_order ON order_detail(order_id);
CREATE INDEX idx_order_detail_batch ON order_detail(batch_id);

CREATE TABLE order_status_history (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status  order_status,
    to_status    order_status NOT NULL,
    changed_by   BIGINT REFERENCES users(id) ON DELETE SET NULL,
    note         TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_order_status_hist_order ON order_status_history(order_id, created_at DESC);

CREATE TABLE promotion_usage (
    id            BIGSERIAL PRIMARY KEY,
    promotion_id  BIGINT NOT NULL REFERENCES promotion(id) ON DELETE CASCADE,
    user_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id      BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    used_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (promotion_id, order_id)
);
CREATE INDEX idx_promo_usage_user ON promotion_usage(user_id, promotion_id);
