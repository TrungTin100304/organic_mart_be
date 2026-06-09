-- V19: Meal Plan Infrastructure
-- Creates meal_plan, meal, and meal_product tables.
-- No impact on existing data.

-- ─── MealPlan ─────────────────────────────────────────────────────────────────
CREATE TABLE meal_plan (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    start_date DATE,
    number_of_days INT NOT NULL DEFAULT 3,
    meals_per_day INT NOT NULL DEFAULT 3,
    servings INT NOT NULL DEFAULT 1,
    diet_type VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    daily_calorie_target INT,
    budget_max DECIMAL(12, 2),
    max_cooking_minutes INT,
    additional_notes TEXT,
    preferences JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    user_allergens JSONB,
    excluded_ingredients JSONB,
    total_calories_per_day INT,
    total_protein_per_day INT,
    total_carbs_per_day INT,
    total_fat_per_day INT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meal_plan_user ON meal_plan (user_id);
CREATE INDEX idx_meal_plan_status ON meal_plan (status);
CREATE INDEX idx_meal_plan_created ON meal_plan (created_at DESC);

-- ─── Meal ─────────────────────────────────────────────────────────────────────
CREATE TABLE meal (
    id BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL REFERENCES meal_plan(id) ON DELETE CASCADE,
    day_number INT NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    ingredients JSONB,
    cooking_instructions TEXT,
    preparation_minutes INT,
    cooking_minutes INT,
    calories INT,
    protein_grams DECIMAL(8, 2),
    carbs_grams DECIMAL(8, 2),
    fat_grams DECIMAL(8, 2),
    meal_number INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meal_plan ON meal (meal_plan_id);
CREATE INDEX idx_meal_day ON meal (meal_plan_id, day_number);

-- ─── MealProduct ──────────────────────────────────────────────────────────────
CREATE TABLE meal_product (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL REFERENCES meal(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES product(id) ON DELETE SET NULL,
    original_ingredient_name VARCHAR(300) NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    estimated_price DECIMAL(12, 2),
    is_in_stock BOOLEAN NOT NULL DEFAULT FALSE,
    added_to_cart BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meal_product_meal ON meal_product (meal_id);
CREATE INDEX idx_meal_product_product ON meal_product (product_id);
