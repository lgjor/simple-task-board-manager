-- Migração para adicionar tabela calendar_events
-- Data: 2025-09-28 10:00

-- Verificar se a tabela calendar_events já existe antes de criá-la
CREATE TABLE IF NOT EXISTS calendar_events (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    start_date_time     TIMESTAMP NOT NULL,
    end_date_time       TIMESTAMP NOT NULL,
    all_day             BOOLEAN NOT NULL DEFAULT FALSE,
    event_type          VARCHAR(50) NOT NULL DEFAULT 'CARD',
    priority            VARCHAR(20) NOT NULL DEFAULT 'LOW',
    color               VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    related_entity_id   BIGINT,
    related_entity_type VARCHAR(50), -- Tipo da entidade relacionada (CARD, TASK, etc.)
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_calendar_events_to_cards FOREIGN KEY (related_entity_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Criar índices se não existirem
CREATE INDEX IF NOT EXISTS idx_calendar_events_related_entity ON calendar_events(related_entity_id, related_entity_type);
CREATE INDEX IF NOT EXISTS idx_calendar_events_start_date ON calendar_events(start_date_time);
CREATE INDEX IF NOT EXISTS idx_calendar_events_active ON calendar_events(active);
