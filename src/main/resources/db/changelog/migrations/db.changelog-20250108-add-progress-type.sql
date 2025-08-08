-- Migration: Adicionar coluna progress_type à tabela cards
-- Data: 2025-01-08
-- Descrição: Adiciona suporte a diferentes tipos de progresso nos cards

-- Adicionar coluna progress_type se ela não existir
ALTER TABLE cards ADD COLUMN IF NOT EXISTS progress_type VARCHAR(50) DEFAULT 'PERCENTAGE';

-- Atualizar cards existentes para ter o valor padrão PERCENTAGE
-- (mantém compatibilidade com cards existentes)
UPDATE cards SET progress_type = 'PERCENTAGE' WHERE progress_type IS NULL;

-- Adicionar comentário à coluna para documentação
COMMENT ON COLUMN cards.progress_type IS 'Tipo de progresso do card: NONE, PERCENTAGE, CUSTOM';
