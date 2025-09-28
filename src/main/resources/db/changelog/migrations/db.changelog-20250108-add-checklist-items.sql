-- Migration: Add checklist_items table
-- Author: Aú Desviante - Lucas Godoy
-- Date: 2025-01-08
-- Compatible with H2 Database

-- Create checklist_items table
CREATE TABLE IF NOT EXISTS checklist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Create index for better performance (sem IF NOT EXISTS para compatibilidade H2)
CREATE INDEX idx_checklist_items_card_id ON checklist_items(card_id);
CREATE INDEX idx_checklist_items_order_index ON checklist_items(order_index);

-- Nota: PRAGMA table_info não é suportado pelo H2
-- Use INFORMATION_SCHEMA.COLUMNS para verificar estrutura da tabela
