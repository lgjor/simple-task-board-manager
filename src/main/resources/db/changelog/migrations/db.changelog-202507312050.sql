--liquibase formatted sql
--changeset junior:202507312050
--comment: Add default columns to example board

-- Inserir as 3 colunas padrão para o board de exemplo (ID 1)
INSERT INTO board_columns (id, name, order_index, kind, board_id) VALUES 
(1, 'Inicial', 1, 'INITIAL', 1),
(2, 'Em Andamento', 2, 'PENDING', 1),
(3, 'Finalizado', 3, 'FINAL', 1);

--rollback DELETE FROM board_columns WHERE board_id = 1; 