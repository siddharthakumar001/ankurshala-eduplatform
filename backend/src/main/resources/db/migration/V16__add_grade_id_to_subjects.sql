-- Add grade_id column to subjects table
ALTER TABLE subjects
ADD COLUMN grade_id BIGINT;

-- Update existing subjects to associate them with a default grade (e.g., grade with ID 1)
-- You might need to adjust this based on your actual grade data
UPDATE subjects
SET grade_id = 1
WHERE grade_id IS NULL;

ALTER TABLE subjects
ALTER COLUMN grade_id SET NOT NULL;

ALTER TABLE subjects
ADD CONSTRAINT fk_subjects_grade
FOREIGN KEY (grade_id) REFERENCES grades(id);

-- Add a unique constraint for subject name within a grade
ALTER TABLE subjects
ADD CONSTRAINT uk_subjects_grade_name UNIQUE (grade_id, name);

-- Create index for performance
CREATE INDEX idx_subjects_grade_id ON subjects(grade_id);
