-- =====================================================
-- V35: Session Companion Tables for Live Class Support
-- Feature 4: Live Class Companion (before/during/after)
-- Created: January 9, 2026
-- =====================================================

-- Session companion artifact for booking sessions
CREATE TABLE IF NOT EXISTS session_companion (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT,
    topic_id BIGINT,
    subject_id BIGINT,
    
    -- Pre-session content
    pre_session_plan_md TEXT,
    warmup_quiz_id BIGINT,
    warmup_completed BOOLEAN DEFAULT FALSE,
    
    -- Live session content
    live_notes_md TEXT,
    -- Note: session_highlights and questions_asked moved to collection tables
    
    -- Post-session content
    post_session_summary_md TEXT,
    homework_plan_md TEXT,
    -- Note: recommended_topics moved to collection table
    mastery_suggestions JSONB, -- Mastery update suggestions
    
    -- Status tracking
    status VARCHAR(50) DEFAULT 'CREATED',
    prep_generated_at TIMESTAMP,
    live_started_at TIMESTAMP,
    post_generated_at TIMESTAMP,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_session_companion_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_companion_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_session_companion_teacher FOREIGN KEY (teacher_id) REFERENCES users(id),
    CONSTRAINT fk_session_companion_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_session_companion_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_session_companion_warmup_quiz FOREIGN KEY (warmup_quiz_id) REFERENCES quizzes(id)
);

-- Session companion notes (separate entries during live session)
CREATE TABLE IF NOT EXISTS session_companion_notes (
    id BIGSERIAL PRIMARY KEY,
    companion_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    
    note_type VARCHAR(50) DEFAULT 'NOTE', -- NOTE, QUESTION, HIGHLIGHT, ACTION_ITEM
    content TEXT NOT NULL,
    timestamp_in_session INTEGER, -- Seconds from session start
    
    ai_response TEXT, -- AI response if it was a question
    is_resolved BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_companion_note_companion FOREIGN KEY (companion_id) REFERENCES session_companion(id) ON DELETE CASCADE,
    CONSTRAINT fk_companion_note_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Collection tables for arrays (using JPA @ElementCollection)
CREATE TABLE IF NOT EXISTS session_companion_highlights (
    companion_id BIGINT NOT NULL,
    highlight TEXT NOT NULL,
    CONSTRAINT fk_companion_highlights FOREIGN KEY (companion_id) REFERENCES session_companion(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS session_companion_questions (
    companion_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    CONSTRAINT fk_companion_questions FOREIGN KEY (companion_id) REFERENCES session_companion(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS session_companion_recommended_topics (
    companion_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    CONSTRAINT fk_companion_recommended_topics FOREIGN KEY (companion_id) REFERENCES session_companion(id) ON DELETE CASCADE
);

-- Indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_session_companion_booking ON session_companion(booking_id);
CREATE INDEX IF NOT EXISTS idx_session_companion_student ON session_companion(student_id);
CREATE INDEX IF NOT EXISTS idx_session_companion_topic ON session_companion(topic_id);
CREATE INDEX IF NOT EXISTS idx_session_companion_status ON session_companion(status);
CREATE INDEX IF NOT EXISTS idx_session_companion_created_at ON session_companion(created_at);
CREATE INDEX IF NOT EXISTS idx_companion_notes_companion ON session_companion_notes(companion_id);
CREATE INDEX IF NOT EXISTS idx_companion_notes_student ON session_companion_notes(student_id);
CREATE INDEX IF NOT EXISTS idx_companion_highlights_companion ON session_companion_highlights(companion_id);
CREATE INDEX IF NOT EXISTS idx_companion_questions_companion ON session_companion_questions(companion_id);
CREATE INDEX IF NOT EXISTS idx_companion_recommended_topics_companion ON session_companion_recommended_topics(companion_id);

-- Trigger to update updated_at
CREATE OR REPLACE FUNCTION update_session_companion_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_session_companion_updated_at ON session_companion;
CREATE TRIGGER update_session_companion_updated_at
    BEFORE UPDATE ON session_companion
    FOR EACH ROW
    EXECUTE FUNCTION update_session_companion_updated_at();

-- Add comment
COMMENT ON TABLE session_companion IS 'Companion artifacts for booked sessions - pre/during/post session support';
COMMENT ON TABLE session_companion_notes IS 'Individual notes captured during live sessions';

