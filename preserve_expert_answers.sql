-- Migration: Preserve Expert Answers When Questions Are Deleted
-- Purpose: Keep expert answer records even when farmers delete questions
-- This allows tracking how many questions an expert has answered

-- Step 1: Make question_id nullable in expert_answers table
ALTER TABLE expert_answers 
MODIFY COLUMN question_id BIGINT NULL;

-- Step 2: Drop the existing foreign key constraint
-- First, find the constraint name
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = 'agriminds_db' 
  AND TABLE_NAME = 'expert_answers' 
  AND COLUMN_NAME = 'question_id'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- The constraint is typically named something like 'expert_answers_ibfk_1'
-- Replace 'expert_answers_ibfk_1' below with the actual constraint name if different

-- Drop the old constraint (use the name found above)
ALTER TABLE expert_answers 
DROP FOREIGN KEY expert_answers_ibfk_1;

-- Step 3: Add new foreign key with ON DELETE SET NULL
ALTER TABLE expert_answers 
ADD CONSTRAINT fk_expert_answers_question 
FOREIGN KEY (question_id) REFERENCES questions(id) 
ON DELETE SET NULL;

-- Verify the change
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    DELETE_RULE
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS USING (CONSTRAINT_NAME, CONSTRAINT_SCHEMA)
WHERE TABLE_SCHEMA = 'agriminds_db' 
  AND TABLE_NAME = 'expert_answers';

-- Now when a question is deleted:
-- - The question_id in expert_answers will be set to NULL
-- - The expert answer record remains in the database
-- - Expert's answered questions count will still include these records
-- - Expert's ratings associated with these answers are also preserved

COMMIT;
