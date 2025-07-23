-- Garante que as tabelas sejam recriadas do zero a cada inicialização,
-- evitando erros de "tabela já existe" e garantindo um ambiente limpo para testes.
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS cards CASCADE;
DROP TABLE IF EXISTS board_columns CASCADE;
DROP TABLE IF EXISTS boards CASCADE;

-- Definição da tabela 'boards'
CREATE TABLE boards (
    -- BIGINT é um bom tipo para IDs.
    -- AUTO_INCREMENT é a diretiva do H2 que gera o ID automaticamente.
    -- PRIMARY KEY garante que a coluna seja a chave primária.
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- VARCHAR(255) é um tamanho padrão para nomes.
    -- NOT NULL garante que todo board tenha um nome.
    name           VARCHAR(255) NOT NULL,

    -- NOT NULL garante que a data de criação seja sempre registrada.
    creation_date  TIMESTAMP NOT NULL
);

-- Definição da tabela 'board_columns'
CREATE TABLE board_columns (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    order_index   INT NOT NULL,
    -- Armazena o enum como uma string.
    kind          VARCHAR(50) NOT NULL,
    -- Chave estrangeira para a tabela 'boards'.
    board_id      BIGINT NOT NULL,

    -- Garante que o board_id se refira a um board existente
    -- e que ao deletar um board, suas colunas sejam deletadas em cascata.
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
    google_task_id     VARCHAR(255), -- Armazena o ID retornado pela API do Google
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