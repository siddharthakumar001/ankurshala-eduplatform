-- Add board_id column to grades table and update existing grades
-- This migration adds board relationship to grades

-- Add board_id column to grades table
ALTER TABLE grades ADD COLUMN board_id BIGINT;

-- Update existing grades to be associated with the first board (assuming board with id=1 exists)
UPDATE grades SET board_id = 1 WHERE board_id IS NULL;

-- Make board_id NOT NULL after updating existing records
ALTER TABLE grades ALTER COLUMN board_id SET NOT NULL;

-- Add foreign key constraint
ALTER TABLE grades ADD CONSTRAINT fk_grades_board FOREIGN KEY (board_id) REFERENCES boards(id);

-- Add unique constraint for grade name per board (grades can have same name across different boards)
ALTER TABLE grades DROP CONSTRAINT IF EXISTS grades_name_key;
ALTER TABLE grades ADD CONSTRAINT uk_grades_board_name UNIQUE (board_id, name);
