-- Database Migration: Add AI tracking to Questions table
-- Run this SQL script to add the ai_generated column

-- Add ai_generated column to track AI vs human expert answers
ALTER TABLE questions 
ADD COLUMN ai_generated BOOLEAN DEFAULT FALSE AFTER answer_text;

-- Add index for faster queries filtering by AI answers
CREATE INDEX idx_questions_ai_generated ON questions(ai_generated);

-- Update existing records to mark them as non-AI (optional, default handles this)
UPDATE questions 
SET ai_generated = FALSE 
WHERE ai_generated IS NULL;

-- Verify the change
DESCRIBE questions;

-- Example query to see AI vs human answers
-- SELECT 
--     category,
--     COUNT(*) as total_questions,
--     SUM(CASE WHEN ai_generated = TRUE THEN 1 ELSE 0 END) as ai_answers,
--     SUM(CASE WHEN ai_generated = FALSE THEN 1 ELSE 0 END) as expert_answers
-- FROM questions
-- WHERE status = 'Answered'
-- GROUP BY category;
