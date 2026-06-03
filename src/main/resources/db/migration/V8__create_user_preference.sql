-- V8__create_user_preference.sql
-- User health preference table for personalized diet and health tracking.

CREATE TABLE user_preference (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL UNIQUE,
    height_cm             DECIMAL(5,2) CHECK (height_cm > 0),
    weight_kg             DECIMAL(5,2) CHECK (weight_kg > 0),
    bmi                   DECIMAL(4,2),
    health_goal           VARCHAR(100),
    diet_type             diet_type DEFAULT 'NORMAL',
    daily_calorie_target  INT,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_preference_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_user_preference_user_id ON user_preference(user_id);
