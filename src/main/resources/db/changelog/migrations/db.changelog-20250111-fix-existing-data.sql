-- Migration: Corrigir dados existentes para compatibilidade com nova lógica
-- Data: 2025-01-11
-- Descrição: Corrige order_index e progressType de cards existentes

-- 1. Corrigir order_index para cards existentes
-- Primeiro, vamos criar uma tabela temporária para armazenar a nova ordem
CREATE TEMPORARY TABLE temp_card_order AS
SELECT 
    c.id,
    c.board_column_id,
    ROW_NUMBER() OVER (PARTITION BY c.board_column_id ORDER BY c.creation_date ASC) as new_order_index
FROM cards c;

-- 2. Atualizar o order_index de todos os cards
UPDATE cards 
SET order_index = (
    SELECT new_order_index 
    FROM temp_card_order 
    WHERE temp_card_order.id = cards.id
);

-- 3. Corrigir ProgressType baseado no tipo do card
-- Cards do tipo CARD (ID 1) devem ter ProgressType.NONE
UPDATE cards 
SET progress_type = 'NONE' 
WHERE card_type_id = 1 AND (progress_type = 'PERCENTAGE' OR progress_type IS NULL);

-- Cards dos tipos BOOK, VIDEO, COURSE (IDs 2, 3, 4) devem ter ProgressType.PERCENTAGE se não definido
UPDATE cards 
SET progress_type = 'PERCENTAGE' 
WHERE card_type_id IN (2, 3, 4) AND (progress_type = 'NONE' OR progress_type IS NULL);

-- 4. Limpar campos de progresso para cards que não devem tê-los
-- Cards com ProgressType.NONE não devem ter total_units ou current_units
UPDATE cards 
SET total_units = NULL, current_units = NULL 
WHERE progress_type = 'NONE' AND (total_units IS NOT NULL OR current_units IS NOT NULL);

-- 5. Garantir que cards com ProgressType.PERCENTAGE tenham valores válidos
UPDATE cards 
SET total_units = COALESCE(total_units, 1), current_units = COALESCE(current_units, 0)
WHERE progress_type = 'PERCENTAGE' AND (total_units IS NULL OR current_units IS NULL);

-- 6. Limpar tabela temporária
DROP TABLE temp_card_order;

-- 7. Verificar integridade dos dados
-- Log das correções realizadas
SELECT 
    'Cards corrigidos' as status,
    COUNT(*) as total_cards,
    COUNT(CASE WHEN progress_type = 'NONE' THEN 1 END) as cards_sem_progresso,
    COUNT(CASE WHEN progress_type = 'PERCENTAGE' THEN 1 END) as cards_com_progresso,
    COUNT(CASE WHEN order_index > 0 THEN 1 END) as cards_com_order_index_valido
FROM cards;
