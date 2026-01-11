-- V32: Student Notes - AI-generated personal notes for students
-- Creates tables for student notes and version history

-- ============================================================================
-- STUDENT NOTES - AI-generated notes artifacts
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_notes (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    
    -- Board scope (CBSE-only for now)
    board VARCHAR(50) NOT NULL DEFAULT 'CBSE',
    
    -- Note metadata
    title VARCHAR(500) NOT NULL,
    format VARCHAR(50) NOT NULL DEFAULT 'SHORT',  -- SHORT, LONG, REVISION_SHEET
    language VARCHAR(20) NOT NULL DEFAULT 'en',   -- en, hi
    
    -- Content
    content_md TEXT NOT NULL,
    
    -- Generation metadata
    generated_by VARCHAR(100),  -- AI model identifier
    chunks_used INTEGER DEFAULT 0,  -- Number of RAG chunks used
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, ARCHIVED
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for efficient querying
CREATE INDEX idx_sn_student_id ON student_notes(student_id);
CREATE INDEX idx_sn_topic_id ON student_notes(topic_id);
CREATE INDEX idx_sn_subject_id ON student_notes(subject_id);
CREATE INDEX idx_sn_board ON student_notes(board);
CREATE INDEX idx_sn_format ON student_notes(format);
CREATE INDEX idx_sn_status ON student_notes(status);
CREATE INDEX idx_sn_student_topic ON student_notes(student_id, topic_id);
CREATE INDEX idx_sn_created_at ON student_notes(created_at);

-- ============================================================================
-- STUDENT NOTES VERSIONS - Version history for notes
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_notes_versions (
    id BIGSERIAL PRIMARY KEY,
    note_id BIGINT NOT NULL REFERENCES student_notes(id) ON DELETE CASCADE,
    
    -- Version info
    version INTEGER NOT NULL,
    content_md TEXT NOT NULL,
    
    -- Generation metadata
    generated_by VARCHAR(100),
    chunks_used INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Ensure unique versions per note
    CONSTRAINT uk_note_version UNIQUE (note_id, version)
);

-- Indexes for version history
CREATE INDEX idx_snv_note_id ON student_notes_versions(note_id);
CREATE INDEX idx_snv_version ON student_notes_versions(note_id, version);

-- ============================================================================
-- TRIGGERS for updated_at timestamp
-- ============================================================================
CREATE TRIGGER trg_student_notes_updated_at
    BEFORE UPDATE ON student_notes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS for documentation
-- ============================================================================
COMMENT ON TABLE student_notes IS 'AI-generated notes artifacts for students, organized by topic and format';
COMMENT ON COLUMN student_notes.format IS 'Note format: SHORT (quick summary), LONG (detailed), REVISION_SHEET (exam prep)';
COMMENT ON COLUMN student_notes.board IS 'Educational board scope, currently CBSE-only';
COMMENT ON COLUMN student_notes.content_md IS 'Markdown-formatted note content';

COMMENT ON TABLE student_notes_versions IS 'Version history for regenerated notes';
COMMENT ON COLUMN student_notes_versions.version IS 'Version number starting from 1';

