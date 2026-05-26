-- V3__create_product_related_tables.sql

-- Allergen table
CREATE TABLE allergen (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    description TEXT

);

-- Product Category table
CREATE TABLE product_category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    slug        VARCHAR(120) UNIQUE NOT NULL,
    parent_id   BIGINT REFERENCES product_category(id) ON DELETE SET NULL,
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_product_category_parent ON product_category(parent_id);

-- Product table
CREATE TABLE product (
    id                    BIGSERIAL PRIMARY KEY,
    category_id           BIGINT NOT NULL REFERENCES product_category(id) ON DELETE RESTRICT,
    name                  VARCHAR(200) NOT NULL,
    slug                  VARCHAR(220) UNIQUE NOT NULL,
    description           TEXT,
    price                 DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    unit                  VARCHAR(20) NOT NULL DEFAULT 'kg',
    nutrition_per_100g    JSONB,
    image_url             TEXT,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_product_category   ON product(category_id);

-- Product-Allergen join table
CREATE TABLE product_allergen (
    product_id   BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    allergen_id  BIGINT NOT NULL REFERENCES allergen(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, allergen_id)
);

