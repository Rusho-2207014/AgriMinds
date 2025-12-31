-- ============================================
-- QUICK FIX: Run this in MySQL Workbench
-- ============================================
-- This adds the missing columns to messages table
-- so that farmer replies can be saved properly
-- ============================================

USE agriminds_db;

-- Add the columns
ALTER TABLE messages 
ADD COLUMN question_id BIGINT NULL AFTER expert_id,
ADD COLUMN expert_answer_id BIGINT NULL AFTER question_id;

-- Add indexes for better performance
ALTER TABLE messages
ADD INDEX idx_question (question_id),
ADD INDEX idx_answer (expert_answer_id);

-- Add foreign key constraints
ALTER TABLE messages
ADD CONSTRAINT fk_message_question 
FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE;

ALTER TABLE messages
ADD CONSTRAINT fk_message_expert_answer 
FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE CASCADE;

-- Verify the changes
DESCRIBE messages;

SELECT 'SUCCESS! Messages table updated. You can now close this and restart your application.' AS Status;
