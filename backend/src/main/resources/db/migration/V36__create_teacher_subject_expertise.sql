-- V30: Create teacher subject expertise table for matching teachers with bookings
-- This table stores the subjects, grades, and boards that a teacher can teach

CREATE TABLE IF NOT EXISTS teacher_subject_expertise (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    grade_id BIGINT REFERENCES grades(id) ON DELETE SET NULL,
    board_id BIGINT REFERENCES boards(id) ON DELETE SET NULL,
    teaching_language VARCHAR(50) DEFAULT 'English',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_teacher_subject_grade_board UNIQUE (teacher_id, subject_id, grade_id, board_id)
);

-- Indexes for efficient querying
CREATE INDEX idx_tse_teacher_id ON teacher_subject_expertise(teacher_id);
CREATE INDEX idx_tse_subject_id ON teacher_subject_expertise(subject_id);
CREATE INDEX idx_tse_grade_id ON teacher_subject_expertise(grade_id);
CREATE INDEX idx_tse_board_id ON teacher_subject_expertise(board_id);
CREATE INDEX idx_tse_active ON teacher_subject_expertise(is_active);

-- Composite index for booking matching queries
CREATE INDEX idx_tse_matching ON teacher_subject_expertise(subject_id, grade_id, board_id, is_active);

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_tse_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_tse_updated_at
    BEFORE UPDATE ON teacher_subject_expertise
    FOR EACH ROW
    EXECUTE FUNCTION update_tse_updated_at();

-- Comment on table and columns
COMMENT ON TABLE teacher_subject_expertise IS 'Maps teachers to subjects they can teach, including grade and board specificity';
COMMENT ON COLUMN teacher_subject_expertise.teacher_id IS 'Reference to the teacher';
COMMENT ON COLUMN teacher_subject_expertise.subject_id IS 'Reference to the subject the teacher can teach';
COMMENT ON COLUMN teacher_subject_expertise.grade_id IS 'Optional: specific grade level (null means all grades)';
COMMENT ON COLUMN teacher_subject_expertise.board_id IS 'Optional: specific board (null means all boards)';
COMMENT ON COLUMN teacher_subject_expertise.teaching_language IS 'Language in which the teacher teaches this subject';
COMMENT ON COLUMN teacher_subject_expertise.is_active IS 'Whether this expertise mapping is currently active';
