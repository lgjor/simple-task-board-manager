-- Migration: Add integration_sync_status table
-- Description: Creates table to track synchronization status between cards and external services
-- Author: Aú Desviante - Lucas Godoy
-- Date: 2025-01-27
-- Compatible with H2 Database

-- Create integration_sync_status table
CREATE TABLE IF NOT EXISTS integration_sync_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    integration_type VARCHAR(50) NOT NULL,
    external_id VARCHAR(255),
    sync_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_sync_date TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint (sem nome para compatibilidade H2)
    FOREIGN KEY (card_id) 
        REFERENCES cards(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate sync status for same card and integration
    UNIQUE (card_id, integration_type)
);

-- Create indexes for better performance (sem IF NOT EXISTS para compatibilidade H2)
CREATE INDEX idx_integration_sync_card_id 
    ON integration_sync_status(card_id);

CREATE INDEX idx_integration_sync_type 
    ON integration_sync_status(integration_type);

CREATE INDEX idx_integration_sync_status 
    ON integration_sync_status(sync_status);

CREATE INDEX idx_integration_sync_last_sync 
    ON integration_sync_status(last_sync_date);
