--liquibase formatted sql
--changeset desviante:202501010000
--comment: Add card type and progress tracking fields to cards table

-- Adicionar campo type para definir o tipo do card (CARD, BOOK, VIDEO, COURSE)
ALTER TABLE cards ADD COLUMN type VARCHAR(50) DEFAULT 'CARD';

-- Adicionar campo total_units para acompanhamento de progresso
ALTER TABLE cards ADD COLUMN total_units INT;

-- Adicionar campo current_units para acompanhamento de progresso
ALTER TABLE cards ADD COLUMN current_units INT;

--rollback ALTER TABLE cards DROP COLUMN type;
--rollback ALTER TABLE cards DROP COLUMN total_units;
--rollback ALTER TABLE cards DROP COLUMN current_units; 