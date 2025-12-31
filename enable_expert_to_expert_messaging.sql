-- Migration to enable expert-to-expert messaging
-- Makes farmer_id nullable so experts can message each other

USE agriminds_db;

-- Make farmer_id nullable to allow expert-to-expert conversations
ALTER TABLE messages 
MODIFY COLUMN farmer_id BIGINT NULL;

-- Update foreign key constraint to allow null farmer_id
-- Note: You may need to drop and recreate the constraint if it exists
-- ALTER TABLE messages DROP FOREIGN KEY fk_message_farmer;
-- ALTER TABLE messages ADD CONSTRAINT fk_message_farmer 
--     FOREIGN KEY (farmer_id) REFERENCES farmers(id) ON DELETE CASCADE;

SELECT 'Migration completed successfully! Expert-to-expert messaging is now enabled.' AS status;
