-- Migração SQLite para adicionar colunas missing e constraints de integridade referencial
-- Data: 2025-09-29 12:00

-- Adicionar colunas missing na tabela cards
-- SQLite não suporta ADD COLUMN IF NOT EXISTS, então vamos tentar adicionar diretamente
-- Se falhar, significa que já existem

-- Adicionar scheduled_date
ALTER TABLE cards ADD COLUMN scheduled_date TIMESTAMP;

-- Adicionar due_date  
ALTER TABLE cards ADD COLUMN due_date TIMESTAMP;

-- Adicionar índices para as novas colunas
CREATE INDEX IF NOT EXISTS idx_cards_scheduled_date ON cards(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(due_date);
CREATE INDEX IF NOT EXISTS idx_cards_urgency ON cards(completion_date, due_date);

-- Adicionar índice para melhorar performance das consultas por entidade relacionada
CREATE INDEX IF NOT EXISTS idx_calendar_events_related_entity 
ON calendar_events(related_entity_type, related_entity_id);

-- SQLite não suporta CHECK constraints complexas ou ADD CONSTRAINT,
-- então vamos focar apenas nas funcionalidades essenciais que são suportadas