-- ================================================
-- V11: AI Recommendations (Personal Trainer)
-- ================================================

CREATE TABLE IF NOT EXISTS ai_recommendations (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id          UUID NOT NULL,
    member_id       UUID NOT NULL,
    workout_plan    TEXT,
    meal_plan       TEXT,
    goals_used      TEXT[],
    experience_level VARCHAR(30),
    created_by      VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),
    is_active       BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_ai_rec_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_rec_gym FOREIGN KEY (gym_id)
        REFERENCES gyms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ai_rec_member    ON ai_recommendations(member_id);
CREATE INDEX IF NOT EXISTS idx_ai_rec_gym       ON ai_recommendations(gym_id);
CREATE INDEX IF NOT EXISTS idx_ai_rec_org       ON ai_recommendations(organisation_id);
CREATE INDEX IF NOT EXISTS idx_ai_rec_member_ts ON ai_recommendations(member_id, created_at DESC);

