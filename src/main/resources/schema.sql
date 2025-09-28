-- Schema seguro que preserva dados existentes
-- Usa CREATE TABLE IF NOT EXISTS para evitar recriação desnecessária

-- Definição da tabela 'board_groups'
CREATE TABLE IF NOT EXISTS board_groups (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    color           VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    icon            VARCHAR(50), -- Ícone do grupo (ex: "work", "personal", "study")
    creation_date   TIMESTAMP NOT NULL
    -- Removido is_default - não precisamos mais de grupo padrão
);

-- Definição da tabela 'boards'
CREATE TABLE IF NOT EXISTS boards (
    -- BIGINT é um bom tipo para IDs.
    -- AUTO_INCREMENT é a diretiva do H2 que gera o ID automaticamente.
    -- PRIMARY KEY garante que a coluna seja a chave primária.
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- VARCHAR(255) é um tamanho padrão para nomes.
    -- NOT NULL garante que todo board tenha um nome.
    name           VARCHAR(255) NOT NULL,

    -- NOT NULL garante que a data de criação seja sempre registrada.
    creation_date  TIMESTAMP NOT NULL,
    
    -- Chave estrangeira para a tabela 'board_groups'.
    -- NULL = board sem grupo específico
    group_id       BIGINT,
    
    -- Garante que o group_id se refira a um grupo existente
    -- e que ao deletar um grupo, os boards fiquem sem grupo (SET NULL).
    CONSTRAINT fk_boards_to_board_groups FOREIGN KEY (group_id) REFERENCES board_groups(id) ON DELETE SET NULL
);

-- Definição da tabela 'board_columns'
CREATE TABLE IF NOT EXISTS board_columns (
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

-- Definição da tabela 'card_types' (para tipos de card)
CREATE TABLE IF NOT EXISTS card_types (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    unit_label      VARCHAR(100) NOT NULL,
    creation_date   TIMESTAMP NOT NULL,
    last_update_date TIMESTAMP NOT NULL
);

-- Definição da tabela 'cards'
CREATE TABLE IF NOT EXISTS cards (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    description       TEXT,
    type              VARCHAR(50) DEFAULT 'CARD',
    total_units       INT,
    current_units     INT,
    creation_date     TIMESTAMP NOT NULL,
    last_update_date  TIMESTAMP NOT NULL,
    completion_date   TIMESTAMP,
    scheduled_date    TIMESTAMP,
    due_date          TIMESTAMP,
    board_column_id   BIGINT NOT NULL,
    card_type_id      BIGINT,
    progress_type     VARCHAR(50) DEFAULT 'PERCENTAGE',
    order_index       INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_cards_to_board_columns FOREIGN KEY (board_column_id) REFERENCES board_columns(id) ON DELETE CASCADE,
    CONSTRAINT fk_cards_to_card_types FOREIGN KEY (card_type_id) REFERENCES card_types(id) ON DELETE SET NULL
);

-- Cria índice para otimizar consultas por coluna e ordem dos cards
CREATE INDEX IF NOT EXISTS idx_cards_column_order ON cards(board_column_id, order_index);
CREATE INDEX IF NOT EXISTS idx_cards_scheduled_date ON cards(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_cards_due_date ON cards(due_date);
CREATE INDEX IF NOT EXISTS idx_cards_urgency ON cards(completion_date, due_date);

-- Definição da tabela 'tasks' (para integração com Google Tasks)
CREATE TABLE IF NOT EXISTS tasks (
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

-- Definição da tabela 'calendar_events' (para persistir eventos do calendário)
CREATE TABLE IF NOT EXISTS calendar_events (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    start_date_time     TIMESTAMP NOT NULL,
    end_date_time       TIMESTAMP NOT NULL,
    all_day             BOOLEAN NOT NULL DEFAULT FALSE,
    event_type          VARCHAR(50) NOT NULL DEFAULT 'CARD',
    priority            VARCHAR(20) NOT NULL DEFAULT 'LOW',
    color               VARCHAR(7), -- Código hex da cor (ex: #FF5733)
    related_entity_id   BIGINT,
    related_entity_type VARCHAR(50), -- Tipo da entidade relacionada (CARD, TASK, etc.)
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_calendar_events_to_cards FOREIGN KEY (related_entity_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Definição da tabela 'integration_sync_status' (para rastrear sincronização com sistemas externos)
CREATE TABLE IF NOT EXISTS integration_sync_status (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_id             BIGINT NOT NULL,
    integration_type    VARCHAR(50) NOT NULL,
    external_id         VARCHAR(255),
    sync_status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_sync_date      TIMESTAMP,
    error_message       TEXT,
    retry_count         INTEGER DEFAULT 0,
    max_retries         INTEGER DEFAULT 3,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE,
    UNIQUE (card_id, integration_type)
);

-- Cria índices para as chaves estrangeiras, melhorando a performance de joins e buscas.
CREATE INDEX IF NOT EXISTS idx_board_columns_board_id ON board_columns(board_id);
CREATE INDEX IF NOT EXISTS idx_cards_board_column_id ON cards(board_column_id);
CREATE INDEX IF NOT EXISTS idx_tasks_card_id ON tasks(card_id);
CREATE INDEX IF NOT EXISTS idx_boards_group_id ON boards(group_id);
CREATE INDEX IF NOT EXISTS idx_calendar_events_related_entity ON calendar_events(related_entity_id, related_entity_type);
CREATE INDEX IF NOT EXISTS idx_calendar_events_start_date ON calendar_events(start_date_time);
CREATE INDEX IF NOT EXISTS idx_calendar_events_active ON calendar_events(active);
CREATE INDEX IF NOT EXISTS idx_integration_sync_card_id ON integration_sync_status(card_id);
CREATE INDEX IF NOT EXISTS idx_integration_sync_type ON integration_sync_status(integration_type);
CREATE INDEX IF NOT EXISTS idx_integration_sync_status ON integration_sync_status(sync_status);
CREATE INDEX IF NOT EXISTS idx_integration_sync_last_sync ON integration_sync_status(last_sync_date);

-- Não inserimos mais grupo padrão - boards sem grupo terão group_id = NULL

-- Dados de exemplo para testes (apenas se não existirem)
-- Inserir um board de exemplo apenas se não existir
INSERT INTO boards (id, name, creation_date) 
SELECT 1, 'Board de Exemplo', CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM boards WHERE id = 1);

-- Inserir as 3 colunas padrão para o board de exemplo (ID 1) apenas se não existirem
INSERT INTO board_columns (id, name, order_index, kind, board_id) 
SELECT 1, 'Inicial', 1, 'INITIAL', 1 
WHERE NOT EXISTS (SELECT 1 FROM board_columns WHERE id = 1);

INSERT INTO board_columns (id, name, order_index, kind, board_id) 
SELECT 2, 'Em Andamento', 2, 'PENDING', 1 
WHERE NOT EXISTS (SELECT 1 FROM board_columns WHERE id = 2);

INSERT INTO board_columns (id, name, order_index, kind, board_id) 
SELECT 3, 'Finalizado', 3, 'FINAL', 1 
WHERE NOT EXISTS (SELECT 1 FROM board_columns WHERE id = 3);

-- Inserir tipos padrão na tabela card_types apenas se não existirem
INSERT INTO card_types (id, name, unit_label, creation_date, last_update_date) 
SELECT 1, 'CARD', 'card', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM card_types WHERE id = 1);

INSERT INTO card_types (id, name, unit_label, creation_date, last_update_date) 
SELECT 2, 'BOOK', 'páginas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM card_types WHERE id = 2);

INSERT INTO card_types (id, name, unit_label, creation_date, last_update_date) 
SELECT 3, 'VIDEO', 'minutos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM card_types WHERE id = 3);

INSERT INTO card_types (id, name, unit_label, creation_date, last_update_date) 
SELECT 4, 'COURSE', 'aulas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM card_types WHERE id = 4);

-- Inserir grupos padrão na tabela board_groups apenas se não existirem
INSERT INTO board_groups (id, name, description, color, icon, creation_date) 
SELECT 1, 'Projetos pessoais', 'Projetos pessoais e hobbies', '#FFEAA7', '1f4bb', CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE id = 1);

INSERT INTO board_groups (id, name, description, color, icon, creation_date) 
SELECT 2, 'Livros', 'Leitura e estudo de livros', '#4ECDC4', '1f4da', CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE id = 2);

INSERT INTO board_groups (id, name, description, color, icon, creation_date) 
SELECT 3, 'Trabalho', 'Tarefas profissionais e trabalho', '#45B7D1', '1f528', CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM board_groups WHERE id = 3);

-- Inserir um card de exemplo na coluna inicial (ID 1) apenas se não existir
INSERT INTO cards (id, title, description, board_column_id, creation_date, last_update_date) 
SELECT 1, 'Card de Exemplo', 'Este é um card de exemplo para demonstrar o sistema', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP 
WHERE NOT EXISTS (SELECT 1 FROM cards WHERE id = 1);