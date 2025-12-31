-- Add acceptance status for corrections/additions/replies
-- NULL = regular answer (no approval needed)
-- TRUE = accepted correction
-- FALSE = denied correction

ALTER TABLE expert_answers 
ADD COLUMN accepted BOOLEAN DEFAULT NULL COMMENT 'NULL for regular answers, TRUE for accepted corrections, FALSE for denied';

-- Add index for faster queries on accepted corrections
CREATE INDEX idx_accepted ON expert_answers(accepted);
