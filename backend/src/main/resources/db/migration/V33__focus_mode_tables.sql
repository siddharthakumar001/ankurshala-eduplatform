-- V33: Focus Mode + Study Sprints - AI Coach Loop
-- Creates tables for focus settings, sessions, and check-ins

-- ============================================================================
-- STUDENT FOCUS SETTINGS - Per-student focus mode preferences
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_focus_settings (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Focus mode preferences
    focus_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    default_sprint_minutes INTEGER NOT NULL DEFAULT 15,  -- 10, 15, 20 minutes
    language_pref VARCHAR(20) NOT NULL DEFAULT 'en',     -- en, hi
    
    -- Notification preferences
    reminder_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    reminder_before_minutes INTEGER NOT NULL DEFAULT 5,
    
    -- Sound preferences
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- One settings record per student
    CONSTRAINT uk_student_focus_settings UNIQUE (student_id)
);

CREATE INDEX idx_sfs_student_id ON student_focus_settings(student_id);

-- ============================================================================
-- STUDENT FOCUS SESSIONS - Active study sessions with goals
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_focus_sessions (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Session context
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    
    -- Session parameters
    goal_text VARCHAR(500) NOT NULL,  -- What the student wants to learn
    sprint_minutes INTEGER NOT NULL DEFAULT 15,
    language VARCHAR(20) NOT NULL DEFAULT 'en',
    
    -- Session status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, PAUSED, ENDED, ABANDONED
    current_step INTEGER NOT NULL DEFAULT 0,
    
    -- Timing
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    paused_at TIMESTAMP,
    ended_at TIMESTAMP,
    total_active_seconds INTEGER DEFAULT 0,
    
    -- Session outcome
    steps_completed INTEGER DEFAULT 0,
    questions_answered INTEGER DEFAULT 0,
    questions_correct INTEGER DEFAULT 0,
    
    -- AI-generated session plan
    session_plan_json JSONB,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sfss_student_id ON student_focus_sessions(student_id);
CREATE INDEX idx_sfss_status ON student_focus_sessions(status);
CREATE INDEX idx_sfss_topic_id ON student_focus_sessions(topic_id);
CREATE INDEX idx_sfss_started_at ON student_focus_sessions(started_at);
CREATE INDEX idx_sfss_student_status ON student_focus_sessions(student_id, status);

-- ============================================================================
-- SPRINT CHECK-INS - Step-by-step check-ins within a session
-- ============================================================================
CREATE TABLE IF NOT EXISTS sprint_checkins (
    id BIGSERIAL PRIMARY KEY,
    focus_session_id BIGINT NOT NULL REFERENCES student_focus_sessions(id) ON DELETE CASCADE,
    
    -- Check-in order
    step_number INTEGER NOT NULL,
    
    -- AI-generated plan for this step
    ai_plan_json JSONB NOT NULL,  -- {explanation, question, suggestedActions, topicHints}
    
    -- Student response
    student_response_text TEXT,
    response_correct BOOLEAN,
    
    -- Timing
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    responded_at TIMESTAMP,
    time_spent_seconds INTEGER,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Ensure unique step numbers per session
    CONSTRAINT uk_session_step UNIQUE (focus_session_id, step_number)
);

CREATE INDEX idx_sc_session_id ON sprint_checkins(focus_session_id);
CREATE INDEX idx_sc_step ON sprint_checkins(focus_session_id, step_number);

-- ============================================================================
-- TRIGGERS for updated_at timestamp
-- ============================================================================
CREATE TRIGGER trg_student_focus_settings_updated_at
    BEFORE UPDATE ON student_focus_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_student_focus_sessions_updated_at
    BEFORE UPDATE ON student_focus_sessions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS for documentation
-- ============================================================================
COMMENT ON TABLE student_focus_settings IS 'Per-student focus mode preferences and settings';
COMMENT ON COLUMN student_focus_settings.default_sprint_minutes IS 'Default sprint duration: 10, 15, or 20 minutes';

COMMENT ON TABLE student_focus_sessions IS 'Active study sessions with goals and AI coaching';
COMMENT ON COLUMN student_focus_sessions.status IS 'Session status: ACTIVE, PAUSED, ENDED, ABANDONED';
COMMENT ON COLUMN student_focus_sessions.session_plan_json IS 'AI-generated study plan for the session';

COMMENT ON TABLE sprint_checkins IS 'Step-by-step AI check-ins within a focus session';
COMMENT ON COLUMN sprint_checkins.ai_plan_json IS 'AI-generated content for this step including explanation and questions';

