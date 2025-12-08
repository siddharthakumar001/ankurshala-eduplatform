-- Populate initial grades 7-12 for all existing boards
-- Note: V13 already added board_id column, constraints, and indexes
-- This migration only adds the grade data
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
