-- Inserir tipos padrão na tabela card_types
INSERT INTO card_types (name, unit_label, creation_date, last_update_date) VALUES 
('CARD', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('BOOK', 'páginas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VIDEO', 'minutos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('COURSE', 'aulas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP); 