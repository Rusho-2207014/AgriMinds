-- Migration: Add Expert Reply/Correction Feature
-- Purpose: Allow experts to reply to or correct other experts' answers

-- Add parent_answer_id column to track replies/corrections
ALTER TABLE expert_answers 
ADD COLUMN parent_answer_id BIGINT NULL AFTER expert_name,
ADD COLUMN reply_type VARCHAR(20) NULL AFTER parent_answer_id,
ADD CONSTRAINT fk_expert_answers_parent 
    FOREIGN KEY (parent_answer_id) REFERENCES expert_answers(id) 
    ON DELETE CASCADE;

-- Add index for faster lookups of replies
CREATE INDEX idx_parent_answer_id ON expert_answers(parent_answer_id);

-- reply_type can be: NULL (original answer), 'correction', 'addition', 'reply'

COMMIT;
