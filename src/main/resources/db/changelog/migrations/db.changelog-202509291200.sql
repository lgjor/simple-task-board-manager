-- Migração para adicionar colunas missing e constraints de integridade referencial
-- Data: 2025-09-29 12:00

-- Adicionar colunas missing na tabela cards (se não existirem)
-- Essas colunas existem no schema de teste mas não no de produção
ALTER TABLE cards ADD COLUMN IF NOT EXISTS scheduled_date TIMESTAMP;
ALTER TABLE cards ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;

-- Adicionar índices para as novas colunas (se não existirem)
CREATE INDEX IF NOT EXISTS idx_cards_scheduled_date ON cards(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(due_date);
CREATE INDEX IF NOT EXISTS idx_cards_urgency ON cards(completion_date, due_date);

-- Adicionar constraint de integridade referencial para calendar_events
-- Garante que quando um card é deletado, os eventos relacionados também sejam removidos
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_calendar_events_cards' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT fk_calendar_events_cards 
        FOREIGN KEY (related_entity_id) 
        REFERENCES cards(id) 
        ON DELETE CASCADE;
    END IF;
END $$;

-- Adicionar índice para melhorar performance das consultas por entidade relacionada
CREATE INDEX IF NOT EXISTS idx_calendar_events_related_entity 
ON calendar_events(related_entity_type, related_entity_id);

-- Adicionar constraint para garantir que related_entity_type seja válido
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_entity_type' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_entity_type 
        CHECK (related_entity_type IN ('CARD', 'TASK', 'CUSTOM'));
    END IF;
END $$;

-- Adicionar constraint para garantir que event_type seja válido
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_event_type' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_event_type 
        CHECK (event_type IN ('CARD', 'TASK', 'CUSTOM', 'MEETING', 'DEADLINE'));
    END IF;
END $$;

-- Adicionar constraint para garantir que priority seja válida
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_priority' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_priority 
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'));
    END IF;
END $$;

-- Adicionar constraint para garantir que all_day seja sempre definido
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_all_day' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_all_day 
        CHECK (all_day IN (TRUE, FALSE));
    END IF;
END $$;

-- Adicionar constraint para garantir que active seja sempre definido
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_active' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_active 
        CHECK (active IN (TRUE, FALSE));
    END IF;
END $$;

-- Adicionar constraint para garantir que start_date_time seja anterior a end_date_time
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_date_order' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_date_order 
        CHECK (start_date_time <= end_date_time);
    END IF;
END $$;

-- Adicionar constraint para garantir que related_entity_id seja positivo quando não nulo
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_entity_id' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_entity_id 
        CHECK (related_entity_id IS NULL OR related_entity_id > 0);
    END IF;
END $$;

-- Adicionar constraint para garantir que color seja um código hex válido quando não nulo
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_color' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_color 
        CHECK (color IS NULL OR color ~ '^#[0-9A-Fa-f]{6}$');
    END IF;
END $$;

-- Adicionar constraint para garantir que title não seja vazio
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_title' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_title 
        CHECK (LENGTH(TRIM(title)) > 0);
    END IF;
END $$;

-- Adicionar constraint para garantir que related_entity_type seja obrigatório quando related_entity_id não for nulo
-- Primeiro verifica se a constraint já existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_calendar_events_entity_type_required' 
        AND table_name = 'calendar_events'
    ) THEN
        ALTER TABLE calendar_events 
        ADD CONSTRAINT chk_calendar_events_entity_type_required 
        CHECK ((related_entity_id IS NULL AND related_entity_type IS NULL) OR 
               (related_entity_id IS NOT NULL AND related_entity_type IS NOT NULL));
    END IF;
END $$;