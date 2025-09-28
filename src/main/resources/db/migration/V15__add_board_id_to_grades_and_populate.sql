-- Add board_id column to grades table and populate with initial data
-- This migration adds board relationship to grades and populates initial grades

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

-- Insert initial grades 7-12 for all existing boards
INSERT INTO grades (name, display_name, board_id, active, created_at, updated_at)
SELECT 
    grade_num.name,
    grade_num.display_name,
    b.id as board_id,
    true as active,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM (
    VALUES 
        ('7', 'Grade 7'),
        ('8', 'Grade 8'),
        ('9', 'Grade 9'),
        ('10', 'Grade 10'),
        ('11', 'Grade 11'),
        ('12', 'Grade 12')
) AS grade_num(name, display_name)
CROSS JOIN boards b
WHERE NOT EXISTS (
    SELECT 1 FROM grades g 
    WHERE g.name = grade_num.name 
    AND g.board_id = b.id
);
