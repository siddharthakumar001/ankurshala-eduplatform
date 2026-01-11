-- V34: Daily Practice Loop - Weakness-based micro-quizzes
-- Creates tables for practice preferences and daily practice queue

-- ============================================================================
-- STUDENT PRACTICE PREFERENCES - Daily practice settings
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_practice_preferences (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Practice settings
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    daily_question_count INTEGER NOT NULL DEFAULT 5,  -- 3, 5, or 10
    preferred_time_local VARCHAR(5),  -- HH:mm format, e.g., "08:00"
    language VARCHAR(20) NOT NULL DEFAULT 'en',
    
    -- Notification settings
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- One preferences record per student
    CONSTRAINT uk_student_practice_preferences UNIQUE (student_id)
);

CREATE INDEX idx_spp_student_id ON student_practice_preferences(student_id);
CREATE INDEX idx_spp_enabled ON student_practice_preferences(enabled);

-- ============================================================================
-- PRACTICE SESSIONS - AI-generated practice sessions with quiz tracking
-- (Note: daily_practice_queue table is created in V30__create_today_home_tables.sql)
-- ============================================================================
CREATE TABLE IF NOT EXISTS practice_sessions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    
    -- Scheduling
    scheduled_for_date DATE NOT NULL,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    
    -- Quiz reference (when quiz is generated)
    quiz_id BIGINT REFERENCES quizzes(id) ON DELETE SET NULL,
    
    -- Results
    score DECIMAL(5,2),
    questions_answered INTEGER,
    questions_correct INTEGER,
    mastery_delta DECIMAL(4,3),  -- Change in mastery from this practice
    
    -- Timing
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    time_spent_seconds INTEGER,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ps_student_id ON practice_sessions(student_id);
CREATE INDEX idx_ps_topic_id ON practice_sessions(topic_id);
CREATE INDEX idx_ps_scheduled_date ON practice_sessions(scheduled_for_date);
CREATE INDEX idx_ps_status ON practice_sessions(status);
CREATE INDEX idx_ps_student_date ON practice_sessions(student_id, scheduled_for_date);
CREATE INDEX idx_ps_student_status ON practice_sessions(student_id, status);

-- ============================================================================
-- TRIGGERS for updated_at timestamp
-- ============================================================================
CREATE TRIGGER trg_student_practice_preferences_updated_at
    BEFORE UPDATE ON student_practice_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_practice_sessions_updated_at
    BEFORE UPDATE ON practice_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS for documentation
-- ============================================================================
COMMENT ON TABLE student_practice_preferences IS 'Per-student daily practice preferences';
COMMENT ON COLUMN student_practice_preferences.daily_question_count IS 'Number of questions per day: 3, 5, or 10';
COMMENT ON COLUMN student_practice_preferences.preferred_time_local IS 'Preferred practice time in HH:mm format';

COMMENT ON TABLE practice_sessions IS 'AI-generated practice sessions with quiz tracking';
COMMENT ON COLUMN practice_sessions.status IS 'Practice status: PENDING, IN_PROGRESS, COMPLETED, SKIPPED';
COMMENT ON COLUMN practice_sessions.mastery_delta IS 'Change in mastery score after completing practice';

