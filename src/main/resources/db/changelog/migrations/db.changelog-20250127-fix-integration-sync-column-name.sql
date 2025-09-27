-- Migration: Fix integration_sync_status column name
-- Description: Renames last_sync_at to last_sync_date to match the model
-- Author: Aú Desviante - Lucas Godoy
-- Date: 2025-01-27

-- For H2 database, we need to recreate the table with the correct column name
-- First, create a backup table
CREATE TABLE IF NOT EXISTS integration_sync_status_backup AS 
SELECT * FROM integration_sync_status;

-- Drop the original table
DROP TABLE IF EXISTS integration_sync_status;

-- Recreate the table with the correct column name
CREATE TABLE integration_sync_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    integration_type VARCHAR(50) NOT NULL,
    external_id VARCHAR(255),
    sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_sync_date TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_integration_sync_card 
        FOREIGN KEY (card_id) 
        REFERENCES cards(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate sync status for same card and integration
    CONSTRAINT uk_integration_sync_card_type 
        UNIQUE (card_id, integration_type)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_integration_sync_card_id 
    ON integration_sync_status(card_id);

CREATE INDEX IF NOT EXISTS idx_integration_sync_type 
    ON integration_sync_status(integration_type);

CREATE INDEX IF NOT EXISTS idx_integration_sync_status 
    ON integration_sync_status(sync_status);

CREATE INDEX IF NOT EXISTS idx_integration_sync_last_sync 
    ON integration_sync_status(last_sync_date);

-- Copy data from backup table, handling the column name difference
INSERT INTO integration_sync_status (id, card_id, integration_type, external_id, sync_status, last_sync_date, error_message, created_at, updated_at)
SELECT id, card_id, integration_type, external_id, sync_status, 
       CASE 
           WHEN last_sync_at IS NOT NULL THEN last_sync_at
           ELSE NULL
       END as last_sync_date,
       error_message, created_at, updated_at
FROM integration_sync_status_backup;

-- Drop the backup table
DROP TABLE integration_sync_status_backup;
