--liquibase formatted sql
--changeset junior:202501010007
--comment: Adiciona campo order_index na tabela cards para controle de posicionamento

-- Adiciona a coluna order_index na tabela cards
ALTER TABLE cards ADD COLUMN order_index INTEGER DEFAULT 0;

-- Cria índice para otimizar consultas por coluna e ordem
CREATE INDEX idx_cards_column_order ON cards(board_column_id, order_index);

-- Popula o campo order_index baseado na data de criação para cards existentes
-- Garante que cards mais antigos tenham índices menores
UPDATE cards SET order_index = (
    SELECT ROW_NUMBER() OVER (
        PARTITION BY board_column_id 
        ORDER BY creation_date ASC, id ASC
    )
    FROM cards c2 
    WHERE c2.id = cards.id
);

--rollback DROP INDEX idx_cards_column_order;
--rollback ALTER TABLE cards DROP COLUMN order_index;
