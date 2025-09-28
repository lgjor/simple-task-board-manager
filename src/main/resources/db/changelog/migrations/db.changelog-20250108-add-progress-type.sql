-- Migration: Adicionar coluna progress_type à tabela cards
-- Data: 2025-01-08
-- Descrição: Adiciona suporte a diferentes tipos de progresso nos cards
-- Compatible with H2 Database

-- Adicionar coluna progress_type se ela não existir
ALTER TABLE cards ADD COLUMN IF NOT EXISTS progress_type VARCHAR(50) DEFAULT 'PERCENTAGE';

-- Atualizar cards existentes para ter o valor padrão PERCENTAGE
-- (mantém compatibilidade com cards existentes)
UPDATE cards SET progress_type = 'PERCENTAGE' WHERE progress_type IS NULL;

-- Nota: COMMENT ON não é suportado pelo H2
-- A documentação está nos comentários SQL acima
-- Tipo de progresso do card: NONE, PERCENTAGE, CUSTOM
