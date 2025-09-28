-- Populate grades 7 to 12
-- This migration adds default grades for the educational system

INSERT INTO grades (name, display_name, active, created_at, updated_at) VALUES
('7', 'Grade 7', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('8', 'Grade 8', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('9', 'Grade 9', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('10', 'Grade 10', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('11', 'Grade 11', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('12', 'Grade 12', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name) DO NOTHING;
