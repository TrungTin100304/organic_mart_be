-- V7__create_cart_tables.sql
-- Shopping cart tables for authenticated users.

CREATE TABLE cart (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE cart_item (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    quantity    DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id)
        REFERENCES cart(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_cart_item_cart_product UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX idx_cart_item_product_id ON cart_item(product_id);
