-- Migração para adicionar sistema de rastreamento de sincronização
-- Data: 2025-01-20
-- Descrição: Adiciona tabela para rastrear status de sincronização entre cards e sistemas externos

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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_integration_sync_card FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    CONSTRAINT unique_card_integration UNIQUE (card_id, integration_type),
    CONSTRAINT chk_sync_status CHECK (sync_status IN ('SYNCED', 'PENDING', 'ERROR', 'RETRY')),
    CONSTRAINT chk_integration_type CHECK (integration_type IN ('GOOGLE_TASKS', 'CALENDAR'))
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_integration_sync_card_id ON integration_sync_status(card_id);
CREATE INDEX IF NOT EXISTS idx_integration_sync_type ON integration_sync_status(integration_type);
CREATE INDEX IF NOT EXISTS idx_integration_sync_status ON integration_sync_status(sync_status);
CREATE INDEX IF NOT EXISTS idx_integration_sync_external_id ON integration_sync_status(external_id);
CREATE INDEX IF NOT EXISTS idx_integration_sync_retry ON integration_sync_status(retry_count, max_retries);

-- Comentários para documentação
COMMENT ON TABLE integration_sync_status IS 'Rastreia o status de sincronização entre cards locais e sistemas externos';
COMMENT ON COLUMN integration_sync_status.card_id IS 'ID do card local sendo sincronizado';
COMMENT ON COLUMN integration_sync_status.integration_type IS 'Tipo de integração (GOOGLE_TASKS, CALENDAR)';
COMMENT ON COLUMN integration_sync_status.external_id IS 'ID da entidade no sistema externo';
COMMENT ON COLUMN integration_sync_status.sync_status IS 'Status atual da sincronização';
COMMENT ON COLUMN integration_sync_status.retry_count IS 'Número de tentativas de retry realizadas';
COMMENT ON COLUMN integration_sync_status.max_retries IS 'Número máximo de tentativas permitidas';
