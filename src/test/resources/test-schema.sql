-- Garante que as tabelas sejam recriadas do zero a cada inicialização,
-- evitando erros de "tabela já existe" e garantindo um ambiente limpo para testes.
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS board_columns CASCADE;
DROP TABLE IF EXISTS boards CASCADE;
DROP TABLE IF EXISTS board_groups CASCADE;

-- Definição da tabela 'board_groups' (deve vir primeiro por causa das foreign keys)
CREATE TABLE board_groups (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    color           VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    icon            VARCHAR(50), -- Ícone do grupo (ex: "work", "personal", "study")
    creation_date   TIMESTAMP NOT NULL
    -- Removido is_default - não precisamos mais de grupo padrão
);

-- Definição da tabela 'boards'
CREATE TABLE boards (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    creation_date  TIMESTAMP NOT NULL,
    group_id       BIGINT,
    CONSTRAINT fk_boards_to_board_groups FOREIGN KEY (group_id) REFERENCES board_groups(id) ON DELETE SET NULL
);

-- Definição da tabela 'board_columns'
CREATE TABLE board_columns (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    order_index   INT NOT NULL,
    kind          VARCHAR(50) NOT NULL,
    board_id      BIGINT NOT NULL,

    CONSTRAINT fk_board_columns_to_boards FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE
);

-- Definição da tabela 'cards'
CREATE TABLE cards (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    creation_date     TIMESTAMP NOT NULL,
    last_update_date  TIMESTAMP NOT NULL,
    completion_date   TIMESTAMP,
    board_column_id   BIGINT NOT NULL,

    CONSTRAINT fk_cards_to_board_columns FOREIGN KEY (board_column_id) REFERENCES board_columns(id) ON DELETE CASCADE
);

-- Definição da tabela 'tasks' (para integração com Google Tasks)
CREATE TABLE tasks (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    list_title         VARCHAR(255),
    title              VARCHAR(255) NOT NULL,
    due                TIMESTAMP WITH TIME ZONE,
    notes              TEXT,
    google_task_id     VARCHAR(255),
    sent               BOOLEAN NOT NULL DEFAULT FALSE,
    card_id            BIGINT,
    creation_date      TIMESTAMP,
    last_update_date   TIMESTAMP,

    CONSTRAINT fk_tasks_to_cards FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Cria índices para as chaves estrangeiras, melhorando a performance de joins e buscas.
CREATE INDEX idx_board_columns_board_id ON board_columns(board_id);
CREATE INDEX idx_cards_board_column_id ON cards(board_column_id);
CREATE INDEX idx_tasks_card_id ON tasks(card_id);
CREATE INDEX idx_boards_group_id ON boards(group_id);

-- Não inserimos mais grupo padrão - boards sem grupo terão group_id = NULL

-- Dados de exemplo para testes
-- Inserir um board de exemplo
INSERT INTO boards (name, creation_date) VALUES 
('Board de Exemplo', CURRENT_TIMESTAMP);

-- Inserir as 3 colunas padrão para o board de exemplo (ID 1)
INSERT INTO board_columns (name, order_index, kind, board_id) VALUES 
('Inicial', 1, 'INITIAL', 1),
('Em Andamento', 2, 'PENDING', 1),
('Finalizado', 3, 'FINAL', 1);

-- Inserir um card de exemplo na coluna inicial (ID 1)
INSERT INTO cards (title, description, board_column_id, creation_date, last_update_date) VALUES 
('Card de Exemplo', 'Este é um card de exemplo para demonstrar o sistema', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);