-- Migração para adicionar sistema de rastreamento de sincronização
-- Data: 2025-01-20
-- Descrição: Adiciona tabela para rastrear status de sincronização entre cards e sistemas externos
-- Compatível com H2 Database

-- Tabela para rastrear status de sincronização
CREATE TABLE IF NOT EXISTS integration_sync_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id BIGINT NOT NULL,
    integration_type VARCHAR(50) NOT NULL, -- GOOGLE_TASKS, CALENDAR
    external_id VARCHAR(255), -- ID no sistema externo (Google Task ID, Calendar Event ID)
    sync_status VARCHAR(50) NOT NULL, -- SYNCED, PENDING, ERROR, RETRY
    last_sync_date TIMESTAMP,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints (sem nomes para compatibilidade H2)
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE (card_id, integration_type)
);

-- Índices para performance (sem IF NOT EXISTS para compatibilidade H2)
CREATE INDEX idx_integration_sync_card_id ON integration_sync_status(card_id);
CREATE INDEX idx_integration_sync_type ON integration_sync_status(integration_type);
CREATE INDEX idx_integration_sync_status ON integration_sync_status(sync_status);
CREATE INDEX idx_integration_sync_external_id ON integration_sync_status(external_id);
CREATE INDEX idx_integration_sync_retry ON integration_sync_status(retry_count, max_retries);

-- Nota: Comentários COMMENT ON não são suportados pelo H2
-- A documentação está nos comentários SQL acima
