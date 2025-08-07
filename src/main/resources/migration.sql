-- Script de migração para preservar dados durante atualizações
-- Este script deve ser executado apenas quando necessário para adicionar novas tabelas ou colunas

-- Verifica se a tabela card_types existe e a cria se necessário
CREATE TABLE IF NOT EXISTS card_types (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    unit_label      VARCHAR(100) NOT NULL,
    creation_date   TIMESTAMP NOT NULL,
    last_update_date TIMESTAMP NOT NULL
);

-- Verifica se a coluna last_update_date existe em card_types
-- Se não existir, adiciona a coluna
ALTER TABLE card_types ADD COLUMN IF NOT EXISTS last_update_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Verifica se a tabela board_groups existe e a cria se necessário
CREATE TABLE IF NOT EXISTS board_groups (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    color           VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    icon            VARCHAR(50), -- Ícone do grupo (ex: "work", "personal", "study")
    creation_date   TIMESTAMP NOT NULL
);

-- Verifica se a coluna group_id existe em boards
-- Se não existir, adiciona a coluna
ALTER TABLE boards ADD COLUMN IF NOT EXISTS group_id BIGINT;

-- Verifica se a tabela boards existe e a cria se necessário
CREATE TABLE IF NOT EXISTS boards (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    creation_date  TIMESTAMP NOT NULL,
    group_id       BIGINT
);

-- Verifica se a tabela board_columns existe e a cria se necessário
CREATE TABLE IF NOT EXISTS board_columns (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    order_index   INT NOT NULL,
    kind          VARCHAR(50) NOT NULL,
    board_id      BIGINT NOT NULL
);

-- Verifica se a tabela cards existe e a cria se necessário
CREATE TABLE IF NOT EXISTS cards (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    order_index       INT NOT NULL,
    creation_date     TIMESTAMP NOT NULL,
    last_update_date  TIMESTAMP NOT NULL,
    due_date          TIMESTAMP,
    priority          VARCHAR(20) DEFAULT 'MEDIUM',
    status            VARCHAR(50) DEFAULT 'PENDING',
    card_type         VARCHAR(50) DEFAULT 'TASK',
    card_type_id    BIGINT,
    column_id         BIGINT NOT NULL,
    board_id          BIGINT NOT NULL
);

-- Verifica se a tabela tasks existe e a cria se necessário
CREATE TABLE IF NOT EXISTS tasks (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    completed       BOOLEAN DEFAULT FALSE,
    completion_date TIMESTAMP,
    creation_date   TIMESTAMP NOT NULL,
    last_update_date TIMESTAMP NOT NULL,
    due_date        TIMESTAMP,
    priority        VARCHAR(20) DEFAULT 'MEDIUM',
    card_id         BIGINT NOT NULL
);

-- Adiciona foreign key constraints
ALTER TABLE boards ADD CONSTRAINT fk_boards_to_board_groups 
    FOREIGN KEY (group_id) REFERENCES board_groups(id) ON DELETE SET NULL;

ALTER TABLE board_columns ADD CONSTRAINT fk_board_columns_to_boards 
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

ALTER TABLE cards ADD CONSTRAINT fk_cards_to_board_columns 
    FOREIGN KEY (column_id) REFERENCES board_columns(id) ON DELETE CASCADE;

ALTER TABLE cards ADD CONSTRAINT fk_cards_to_boards 
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE;

ALTER TABLE cards ADD CONSTRAINT fk_cards_to_card_types 
    FOREIGN KEY (card_type_id) REFERENCES card_types(id) ON DELETE SET NULL;

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_to_cards 
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE;

-- Verifica se há dados de exemplo para board_groups e insere se necessário
INSERT INTO board_groups (name, description, color, icon, creation_date)
SELECT 'Trabalho', 'Quadros relacionados ao trabalho', '#FF5733', 'work', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE name = 'Trabalho');

INSERT INTO board_groups (name, description, color, icon, creation_date)
SELECT 'Pessoal', 'Quadros pessoais', '#33FF57', 'personal', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE name = 'Pessoal');

INSERT INTO board_groups (name, description, color, icon, creation_date)
SELECT 'Estudo', 'Quadros de estudo e aprendizado', '#3357FF', 'study', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE name = 'Estudo'); 