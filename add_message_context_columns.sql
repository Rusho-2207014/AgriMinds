-- Add question and answer context columns to messages table for threaded conversations

ALTER TABLE messages 
ADD COLUMN question_id BIGINT NULL AFTER expert_id,
ADD COLUMN expert_answer_id BIGINT NULL AFTER question_id,
ADD INDEX idx_question (question_id),
ADD INDEX idx_answer (expert_answer_id);

-- Add foreign key constraints
ALTER TABLE messages
ADD CONSTRAINT fk_message_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_message_expert_answer FOREIGN KEY (expert_answer_id) REFERENCES expert_answers(id) ON DELETE CASCADE;
