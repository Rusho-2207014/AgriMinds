-- Add sender_name column to messages table for better messaging display
-- This allows showing the actual name of the sender in message bubbles

ALTER TABLE messages ADD COLUMN IF NOT EXISTS sender_name VARCHAR(100) AFTER sender_type;

-- Update existing messages with sender names from farmers table
UPDATE messages m
INNER JOIN farmers f ON m.farmer_id = f.id
SET m.sender_name = f.full_name
WHERE m.sender_type = 'FARMER' AND m.sender_name IS NULL;

-- Update existing messages with sender names from experts table
UPDATE messages m
INNER JOIN experts e ON m.expert_id = e.id
SET m.sender_name = e.full_name
WHERE m.sender_type = 'EXPERT' AND m.sender_name IS NULL;
