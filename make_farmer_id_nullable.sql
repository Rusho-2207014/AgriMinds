-- Migration: Make farmer_id nullable to support expert-to-expert messaging
-- Date: 2025-12-29

ALTER TABLE messages 
MODIFY COLUMN farmer_id BIGINT NULL;

-- Verify the change
DESCRIBE messages;
