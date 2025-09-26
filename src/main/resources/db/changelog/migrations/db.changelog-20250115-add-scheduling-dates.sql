-- Adiciona campos de agendamento e vencimento aos cards
-- Data: 2025-01-15
-- Descrição: Adiciona suporte a data de agendamento (scheduled_date) e data de vencimento (due_date) aos cards
-- IMPORTANTE: Esta migração é segura e não apaga dados existentes

-- Adicionar coluna de data de agendamento
ALTER TABLE cards ADD COLUMN scheduled_date TIMESTAMP NULL;

-- Adicionar coluna de data de vencimento  
ALTER TABLE cards ADD COLUMN due_date TIMESTAMP NULL;

-- Criar índices para melhorar performance das consultas por data
CREATE INDEX idx_cards_scheduled_date ON cards(scheduled_date);
CREATE INDEX idx_cards_due_date ON cards(due_date);

-- Criar índice composto para consultas de urgência
-- Este índice ajuda nas consultas de cards próximos do vencimento
CREATE INDEX idx_cards_urgency ON cards(completion_date, due_date);