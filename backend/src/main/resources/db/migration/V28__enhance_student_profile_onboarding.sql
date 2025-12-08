-- Add onboarding fields to student_profiles
ALTER TABLE student_profiles
ADD COLUMN board_id BIGINT,
ADD COLUMN grade_id BIGINT,
ADD COLUMN language VARCHAR(50),
ADD COLUMN goals VARCHAR(1000),
ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN is_complete BOOLEAN NOT NULL DEFAULT false;

-- Add foreign key constraints
ALTER TABLE student_profiles
ADD CONSTRAINT fk_student_board FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_student_grade FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE SET NULL;

-- Create indexes for lookups
CREATE INDEX idx_student_board ON student_profiles(board_id);
CREATE INDEX idx_student_grade ON student_profiles(grade_id);
CREATE INDEX idx_student_is_complete ON student_profiles(is_complete);

-- Add comments
COMMENT ON COLUMN student_profiles.board_id IS 'Educational board for content filtering';
COMMENT ON COLUMN student_profiles.grade_id IS 'Current grade level for content filtering';
COMMENT ON COLUMN student_profiles.language IS 'Preferred language for instruction';
COMMENT ON COLUMN student_profiles.goals IS 'Student learning goals and objectives';
COMMENT ON COLUMN student_profiles.avatar_url IS 'URL to student avatar image';
COMMENT ON COLUMN student_profiles.is_complete IS 'Whether onboarding is completed';
