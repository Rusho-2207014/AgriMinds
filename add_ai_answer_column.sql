-- Add separate column for AI-generated answers
-- This allows both AI and expert answers to coexist

ALTER TABLE questions ADD COLUMN ai_answer_text TEXT;
ALTER TABLE questions ADD COLUMN ai_answered_date DATETIME;

-- The existing columns remain for expert answers:
-- answer_text - for expert answers
-- answered_by_expert_id - for expert ID
-- expert_name - for expert name
-- answered_date - for expert answer date
-- ai_generated - now indicates if AI answer exists
