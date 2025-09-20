-- Script para verificar a ordenação dos cards no banco de dados
SELECT 
    id,
    title,
    board_column_id,
    order_index,
    creation_date
FROM cards 
ORDER BY board_column_id, order_index ASC;
