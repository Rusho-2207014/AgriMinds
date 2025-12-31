-- Fix rating preservation when questions/answers are deleted
-- Ratings should be preserved even if farmer deletes their question
-- Date: 2025-12-29

-- Step 1: Drop the existing foreign key constraint
ALTER TABLE expert_ratings 
DROP FOREIGN KEY expert_ratings_ibfk_3;

-- Step 2: Make expert_answer_id nullable (allow it to be NULL when answer is deleted)
ALTER TABLE expert_ratings 
MODIFY COLUMN expert_answer_id BIGINT NULL;

-- Step 3: Add new foreign key with SET NULL instead of CASCADE
ALTER TABLE expert_ratings 
ADD CONSTRAINT fk_expert_answer 
FOREIGN KEY (expert_answer_id) 
REFERENCES expert_answers(id) 
ON DELETE SET NULL;

-- Verification: Show the updated table structure
DESCRIBE expert_ratings;

-- Show foreign keys
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME,
    DELETE_RULE,
    UPDATE_RULE
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'expert_ratings' 
AND TABLE_SCHEMA = 'agriminds_db'
AND REFERENCED_TABLE_NAME IS NOT NULL;
